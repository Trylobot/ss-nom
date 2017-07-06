package data.scripts.world.armada;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV2;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.loading.AbilitySpecAPI;
import data.scripts.trylobot.TrylobotUtils;
import data.scripts.world.armada.api.CampaignArmadaAPI;
import java.util.Random;


public class CampaignArmadaController implements EveryFrameScript, CampaignArmadaAPI
{
	// current star system
	private final SectorAPI sector;
	private final LocationAPI spawn_system;
	private final SectorEntityToken spawn_location;
  private MarketAPI market;

	// basic behavior options
	private final String faction_id;
	private final String leader_fleet_id;
	private final String vip_ship_id;
	private final int escort_fleet_count;
	private final String[] escort_fleet_composition_pool;
	private final int[] escort_fleet_composition_weights;
  private final float leash_length;
	//private CampaignArmadaEscortFleetPositionerAPI escort_positioner;
	private final int dead_time_days;
	
	private final CampaignClockAPI clock;
  private final Random random = new Random();
	
	// Refers to the "Armada Leader" - this fleet is the one issued the waypoint
	//  movement orders, and the armada will do its best to protect this fleet.
	//  the leader fleet will travel unwaveringly from waypoint to waypoint
	//  upon entering the system, and the last waypoint will lead it back out of
	//  the system.
	private CampaignFleetAPI leader_fleet = null;
	// Collection of non-leader fleets intended to provide a massive escort for
	//  the fleet leader.
	private CampaignFleetAPI[] escort_fleets = null;
	// 
	private CampaignArmadaWaypointController waypoint_controller = null;
	
	// mini-state machine defs
	private final static int NON_EXISTENT           = 10;
	private final static int JOURNEYING_LIKE_A_BOSS = 20;

	private int state = NON_EXISTENT;
	// used for measuring idle time; defaults to very low value so armada will spawn immediately
	private long last_state_change_timestamp = Long.MIN_VALUE;
	
	private final List _listeners = new LinkedList();
	
	// Constructor also initializes the spawning system and begins spawning fleets
	//  Spawning is immediate and automatic
    //  escort pool and weights assumed to be non-null, non-empty and of equal length
	public CampaignArmadaController( 
		String faction_id,
		String leader_fleet_id,
		String vip_ship_id,
		SectorAPI sector,
		SectorEntityToken spawn_location,
    MarketAPI market,
		int escort_fleet_count,
		String[] escort_fleet_composition_pool,
		int[] escort_fleet_composition_weights, // must total 1000
    float leash_length,
		int waypoints_per_system_minimum,
		int waypoints_per_system_maximum,
		int dead_time_days)
	{
		// setup behaviors; these are not modified by the controller
		this.faction_id = faction_id;
		this.leader_fleet_id = leader_fleet_id;
		this.vip_ship_id = vip_ship_id;
		this.sector = sector;
		this.spawn_location = spawn_location;
		this.escort_fleet_count = escort_fleet_count;
		this.escort_fleet_composition_pool = escort_fleet_composition_pool;
		this.escort_fleet_composition_weights = escort_fleet_composition_weights;
    this.leash_length = leash_length;
		this.dead_time_days = dead_time_days;
		
		this.waypoint_controller = new CampaignArmadaWaypointController(
	  	sector, this, waypoints_per_system_minimum, waypoints_per_system_maximum );
		
		this.spawn_system = spawn_location.getContainingLocation();
		this.clock = sector.getClock();
	}
	
	// API methods
  @Override
	public CampaignFleetAPI getLeaderFleet() { return leader_fleet; }
  
  @Override
	public CampaignFleetAPI[] getEscortFleets() { return escort_fleets; }
	
	//public CampaignArmadaEscortFleetPositionerAPI getEscortFleetPositioner() { return escort_positioner; }
	
	
	private void change_state( int new_state )
	{
		state = new_state;
		last_state_change_timestamp = clock.getTimestamp();
	}
	
	@Override
	public void advance( float amount )
	{
		switch( state )
		{ 
			////////////////////////////////////////
			case NON_EXISTENT:
				float days_dead = clock.getElapsedDaysSince( last_state_change_timestamp );
				if( days_dead >= dead_time_days )
				{
					// create fleets and spawn
          leader_fleet = create_leader_fleet();
					spawn_system.spawnFleet( spawn_location, 0, 0, leader_fleet );
					escort_fleets = create_escort_fleets( leader_fleet );
					for (CampaignFleetAPI escort_fleet : escort_fleets) {
						spawn_system.spawnFleet( spawn_location, 0, 0, escort_fleet );
          }
          // destinations generator
          waypoint_controller.run();
          // events
					change_state( JOURNEYING_LIKE_A_BOSS );
					notifyListeners( "JOURNEYING_LIKE_A_BOSS" );
					TrylobotUtils.debug("armada created");
				}
				break;
			
			////////////////////////////////////////
			case JOURNEYING_LIKE_A_BOSS:
				if( !check_leader() )
				{
					scatter_fleets();
					leader_fleet = null;
					escort_fleets = null;
					change_state( NON_EXISTENT );
					notifyListeners( "NON_EXISTENT" );
					TrylobotUtils.debug("armada leader destroyed; escorts scatter");
					break;
				}
				break;
		}
	}
	
	private CampaignFleetAPI create_leader_fleet()
	{
    CampaignFleetAPI fleet = sector.createFleet( faction_id, leader_fleet_id );
    flesh_out_fleet(fleet);
    fleet.getCargo().addCommodity(Commodities.DRUGS, 250);
    // let the escorts catch up if they fall behind
    fleet.removeAbility("sustained_burn");
    //
    fleet.addScript( new CampaignArmadaOasisRemoteSurveyor( fleet ));
    //
    return fleet;
	}
	
	private CampaignFleetAPI[] create_escort_fleets(CampaignFleetAPI leader_fleet)
	{
		CampaignFleetAPI[] fleets = new CampaignFleetAPI[escort_fleet_count];
		for( int i = 0; i < fleets.length; ++i )
		{
			String fleet_id = weighted_string_pick( 
				escort_fleet_composition_pool,
				escort_fleet_composition_weights );
      //
			CampaignFleetAPI fleet = sector.createFleet( faction_id, fleet_id );
      fleet.setAI( new CampaignArmadaEscortFleetAI( fleet, leader_fleet, fleet.getAI() ));
      //
			flesh_out_fleet(fleet);
      fleet.getCargo().addCommodity(Commodities.DRUGS, 30);
      // they should be able to use this to follow
      fleet.addAbility("fracture_jump");
      //
      fleet.addAssignment(
        FleetAssignment.FOLLOW, leader_fleet, Float.MAX_VALUE); // forever
      
      fleet.addScript( new CampaignArmadaEscortLeashScript( sector, fleet, leader_fleet, leash_length ));
      
      fleets[i] = fleet;
		}
		return fleets;
	}
  
  private void flesh_out_fleet(CampaignFleetAPI fleet) {
    //
    fleet.setMarket(market);
    //
    for (String id : Global.getSettings().getSortedAbilityIds()) {
      AbilitySpecAPI spec = Global.getSettings().getAbilitySpec(id);
      if (spec.isAIDefault()) {
        fleet.addAbility(id);
      }
    }
    //
    FleetFactoryV2.addCommanderAndOfficers(4, 5.0f,10.0f, fleet, null, random);
    //
    fleet.forceSync();
    for (FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
			member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
		}
  }
	
	private boolean check_leader()
	{
    return (leader_fleet != null && leader_fleet.isAlive()
		  && (vip_ship_id == null || find_vip_ship( leader_fleet )));
	}
	
	private boolean find_vip_ship( CampaignFleetAPI fleet )
	{
		if( vip_ship_id == null )
			return false;
		for (FleetMemberAPI ship : fleet.getFleetData().getMembersInPriorityOrder())
    {
			if( vip_ship_id.equals( ship.getHullId() ))
				return true;
		}
		return false;
	}
	
	private void scatter_fleets()
	{
		for (CampaignFleetAPI escort_fleet : escort_fleets)
		{
			if( escort_fleet.isAlive() )
			{
        escort_fleet.clearAssignments();
				// kill everyone aaghhhh!
				escort_fleet.addAssignment( 
					FleetAssignment.RAID_SYSTEM,
					escort_fleet.getContainingLocation().createToken( 0, 0 ), 
					Float.MAX_VALUE );
			}
		}					
	}
	
	private String weighted_string_pick( String[] pool, int[] weights )
	{
		int len = Math.min( pool.length, weights.length );
		int sum = 0;
		for( int i = 0; i < len; ++i )
		{
			sum += weights[i];
		}
		int marker = (int)(Math.random() * (sum + 1)); // pick
		sum = 0;
		for( int i = 0; i < len; ++i )
		{
			sum += weights[i];
			if( marker <= sum )
			{
				return pool[i];
			}
		}
		return null;
	}
	
	// API methods must be defined
  @Override
	public boolean isDone()
	{
		return false; // never done
	}

  @Override
	public boolean runWhilePaused()
	{
		return false; // do not do this
	}
	
	///// events
	
	public class CampaignArmadaControllerEvent
	{
		public String controller_state;
	}
	
	public interface CampaignArmadaControllerEventListener
	{
		public void handle_event( CampaignArmadaControllerEvent event );
	}
	
	public void addListener( CampaignArmadaControllerEventListener listener )
	{
		_listeners.add( listener );
		// send current state immediately
		CampaignArmadaControllerEvent event = new CampaignArmadaControllerEvent();
		switch( state ) { 
			case NON_EXISTENT:           event.controller_state = "NON_EXISTENT";           break;
			case JOURNEYING_LIKE_A_BOSS: event.controller_state = "JOURNEYING_LIKE_A_BOSS"; break;
		}
		listener.handle_event( event );
	}
	
	public void removeListener( CampaignArmadaControllerEventListener listener )
	{
		_listeners.remove( listener );
	}
	
	public void notifyListeners( String controller_state )
	{
		CampaignArmadaControllerEvent event = new CampaignArmadaControllerEvent();
		event.controller_state = controller_state;
		for( Iterator i = _listeners.iterator(); i.hasNext(); )
			((CampaignArmadaControllerEventListener)i.next()).handle_event( event );
	}
	
}

