package data.scripts.world.armada;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import data.scripts._;
import data.scripts.world.armada.api.CampaignArmadaAPI;
import data.scripts.world.armada.api.CampaignArmadaEscortFleetPositionerAPI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;


@SuppressWarnings("unchecked")
public class CampaignArmadaController implements EveryFrameScript, CampaignArmadaAPI
{
	// public constants for arguments
	private final static float STAR_SYSTEM_RADIUS = 15000f; // TODO: remove, spawn explicitly near a specified entity
	
	// current star system
	private SectorAPI sector;
	private LocationAPI spawn_system;
	private SectorEntityToken spawn_location;

	// basic behavior options
	private String faction_id;
	private String leader_fleet_id;
	private int escort_fleet_count;
	private String[] escort_fleet_composition_pool;
	private float[] escort_fleet_composition_weights;
	private CampaignArmadaEscortFleetPositionerAPI escort_positioner;
	private int waypoints_per_system_minimum;
	private int waypoints_per_system_maximum;
	private int waypoint_idle_time_days;
	private float waypoint_achieved_radius;
	private int dead_time_days;
	
	private CampaignClockAPI clock;

	// Refers to the "Armada Leader" - this fleet is the one issued the waypoint
	//  movement orders, and the armada will do its best to protect this fleet.
	//  the leader fleet will travel unwaveringly from waypoint to waypoint
	//  upon entering the system, and the last waypoint will lead it back out of
	//  the system.
	private CampaignFleetAPI leader_fleet = null;
	
	// Collection of non-leader fleets intended to provide a massive escort for
	//  the fleet leader.
	private CampaignFleetAPI[] escort_fleets = null;
	

	// state machine defs
	private final static int NON_EXISTENT                        = 10;
	private final static int SPACE_IN_TRANSIT_TO_WAYPOINT        = 20;
	private final static int SPACE_IDLE_AT_WAYPOINT              = 30;
	private final static int SPACE_IN_TRANSIT_TO_HYPERSPACE_BUOY = 40;
	private final static int PERFORMING_HYPERSPACE_JUMP          = 50;
	private final static int HYPERSPACE_IN_TRANSIT_TO_EXIT       = 60;
	private final static int PERFORMING_JUMP_TO_SYSTEM           = 70;
	// 
	private int state = NON_EXISTENT;
	
	// used for measuring idle time; defaults to very low value so armada will spawn immediately
	private long last_state_change_timestamp = Long.MIN_VALUE;
	
	private LocationAPI current_system = null;
	// currently selected local-space (in-system) waypoints composing a small planned trip for the leader fleet
	private SectorEntityToken[] current_route = null;
	// IN_TRANSIT: next waypoint
	// IDLE_AT_WAYPOINT: current waypoint
	private int current_route_waypoint_index = -1;
	
	// currently selected hyperspace bouy; used when current_route is empty
	private JumpPointAPI current_jump_point = null;
	
	// Constructor also initializes the spawning system and begins spawning fleets
	//  Spawning is immediate and automatic
    //  escort pool and weights assumed to be non-null, non-empty and of equal length
	public CampaignArmadaController( 
		String faction_id,
		String leader_fleet_id,
		SectorAPI sector,
		SectorEntityToken spawn_location,
		int escort_fleet_count,
		String[] escort_fleet_composition_pool,
		float[] escort_fleet_composition_weights,
		CampaignArmadaEscortFleetPositionerAPI escort_positioner,
		int waypoint_per_trip_minimum,
		int waypoint_per_trip_maximum,
		int waypoint_idle_time_days,
		float waypoint_achieved_radius,
		int dead_time_days )
	{
		// setup behaviors; these are not modified by the controller
		this.faction_id = faction_id;
		this.leader_fleet_id = leader_fleet_id;
		this.sector = sector;
		this.spawn_location = spawn_location;
		this.escort_fleet_count = escort_fleet_count;
		this.escort_fleet_composition_pool = escort_fleet_composition_pool;
		this.escort_fleet_composition_weights = escort_fleet_composition_weights;
		this.escort_positioner = escort_positioner;
		this.waypoints_per_system_minimum = waypoint_per_trip_minimum;
		this.waypoints_per_system_maximum = waypoint_per_trip_maximum;
		this.waypoint_idle_time_days = waypoint_idle_time_days;
		this.waypoint_achieved_radius = waypoint_achieved_radius;
		this.dead_time_days = dead_time_days;
		
		this.spawn_system = spawn_location.getContainingLocation();
		this.clock = sector.getClock();
	}
	
	// API methods
	public CampaignFleetAPI getLeaderFleet() { return leader_fleet; }
	public CampaignFleetAPI[] getEscortFleets() { return escort_fleets; }
	
	public CampaignArmadaEscortFleetPositionerAPI getEscortFleetPositioner() { return escort_positioner; }
	
	
	private void change_state( int new_state )
	{
		state = new_state;
		last_state_change_timestamp = clock.getTimestamp();
		_.L("state "+new_state);
	}
	
	@Override
	public void advance( float amount )
	{
		// common prefix actions
		if( state != NON_EXISTENT )
		{
			// check if leader died; if yes, scatter fleet
			check_leader_still_alive();
		}
		switch( state )
		{ 
			////////////////////////////////////////
			case NON_EXISTENT:
				if( clock.getElapsedDaysSince( last_state_change_timestamp ) >= dead_time_days )
				{
					create_armada();
					find_and_order_move_to_hyperspace_buoy();
					change_state( SPACE_IN_TRANSIT_TO_HYPERSPACE_BUOY );
					_.L("moving to hyperspace buoy");
				}
				break;
			
			////////////////////////////////////////
			case SPACE_IN_TRANSIT_TO_HYPERSPACE_BUOY:
				if( get_distance( current_jump_point, leader_fleet ) <= waypoint_achieved_radius )
				{
					order_hyperspace_jump_immediate_all();
					change_state( PERFORMING_HYPERSPACE_JUMP );
					_.L("jumping to hyperspace");
				}
				break;
			
			////////////////////////////////////////
			case PERFORMING_HYPERSPACE_JUMP:
				current_system = leader_fleet.getContainingLocation();
				if( current_system.isHyperspace() && !check_any_fleets_hyperspace_jumping() )
				{
					find_and_order_move_to_hyperspace_buoy();
					change_state( HYPERSPACE_IN_TRANSIT_TO_EXIT );
					_.L("traveling through hyperspace");
				}
				break;
				
			////////////////////////////////////////
			case HYPERSPACE_IN_TRANSIT_TO_EXIT:
				if( get_distance( current_jump_point, leader_fleet ) <= waypoint_achieved_radius )
				{
					order_hyperspace_jump_immediate_all();
					change_state( PERFORMING_JUMP_TO_SYSTEM );
					_.L("exiting hyperspace");
				}
				break;
				
			////////////////////////////////////////
			case PERFORMING_JUMP_TO_SYSTEM:
				current_system = leader_fleet.getContainingLocation();
				if( !current_system.isHyperspace() && !check_any_fleets_hyperspace_jumping() )
				{
					build_space_route();
					order_move_to_next_route_waypoint();
					change_state( SPACE_IN_TRANSIT_TO_WAYPOINT );
					_.L("moving to route waypoint "+current_route_waypoint_index);
				}
				break;
				
			////////////////////////////////////////
			case SPACE_IN_TRANSIT_TO_WAYPOINT:
				if( get_distance( leader_fleet, current_route[current_route_waypoint_index] ) <= waypoint_achieved_radius )
				{
					change_state( SPACE_IDLE_AT_WAYPOINT );
					_.L("idling at waypoint");
				}
				break;

			////////////////////////////////////////
			case SPACE_IDLE_AT_WAYPOINT:
				if( clock.getElapsedDaysSince( last_state_change_timestamp ) >= waypoint_idle_time_days )
				{
					++current_route_waypoint_index;
					order_move_to_next_route_waypoint();
					if( current_route_waypoint_index != -1 )
					{
						change_state( SPACE_IN_TRANSIT_TO_WAYPOINT );
						_.L("moving to route waypoint "+current_route_waypoint_index);
					}
					else // route finished
					{
						find_and_order_move_to_hyperspace_buoy();
						change_state( SPACE_IN_TRANSIT_TO_HYPERSPACE_BUOY );
						_.L("moving to hyperspace buoy");
					}
				}
				break;
		}
		// common postfix actions
		if( state != NON_EXISTENT )
		{
			// keep escort fleets in formation
			update_escort_fleets();
		}
	}
	
	private void create_armada()
	{
		// create & spawn leader fleet
		leader_fleet = create_leader_fleet();
		spawn_system.spawnFleet( spawn_location, 0, 0, leader_fleet );
		// create & spawn escort fleets
		escort_fleets = create_escort_fleets();
		for( int i = 0; i < escort_fleets.length; ++i )
			spawn_system.spawnFleet( spawn_location, 0, 0, escort_fleets[i] );
		current_system = spawn_system;
	}
	
	private void find_and_order_move_to_hyperspace_buoy()
	{
		current_jump_point = choose_jump_point( current_system );
		// proceed to next waypoint
		leader_fleet.clearAssignments();
		leader_fleet.addAssignment(
			FleetAssignment.GO_TO_LOCATION, 
			current_jump_point,
			Float.MAX_VALUE );
	}
	
	private void order_hyperspace_jump_immediate_all()
	{
		// jump to a random destination belonging to the jump buoy
		JumpDestination jump_destination = choose_jump_destination( current_jump_point );
		leader_fleet.clearAssignments();
		sector.doHyperspaceTransition( leader_fleet, null, jump_destination ); // immediate jump
		for( int i = 0; i < escort_fleets.length; ++i )
			sector.doHyperspaceTransition( escort_fleets[i], null, jump_destination);
		// change state to in transit, until the jump buoy is reached
	}
	
	private void build_space_route()
	{
		current_route = choose_system_waypoint_route( current_system );
		current_route_waypoint_index = 0;		
	}
	
	private void order_move_to_next_route_waypoint()
	{
		if( current_route_waypoint_index < current_route.length )
		{
			// proceed to next waypoint
			leader_fleet.clearAssignments();
			leader_fleet.addAssignment(
				FleetAssignment.GO_TO_LOCATION, 
				current_route[current_route_waypoint_index],
				Float.MAX_VALUE );
		}
		else
		{
			current_route_waypoint_index = -1;
		}		
	}
	
	private JumpPointAPI choose_jump_point( LocationAPI location )
	{
		if( location == null )
			return null;
		// find jump points with at least one destination
		List jump_point_pool = new ArrayList();
		List all_jump_points = location.getEntities( JumpPointAPI.class );
		for( Iterator j = all_jump_points.iterator(); j.hasNext(); )
		{
			JumpPointAPI jump_point = (JumpPointAPI)j.next();
			if( !jump_point.getDestinations().isEmpty() )
				jump_point_pool.add( jump_point );
		}
		if( jump_point_pool.isEmpty() )
			return null;
		// pick 1
		return (JumpPointAPI)jump_point_pool.get((int)( Math.random() * jump_point_pool.size() ));
	}
	
	private JumpDestination choose_jump_destination( JumpPointAPI jump_point )
	{
		if( jump_point == null )
			return null;
		List dest_pool = new ArrayList();
		dest_pool.addAll( jump_point.getDestinations() );
		if( dest_pool.isEmpty() )
			return null;
		// pick 1
		return (JumpDestination)dest_pool.get((int)( Math.random() * dest_pool.size() ));
	}
	
	private SectorEntityToken[] choose_system_waypoint_route( LocationAPI location )
	{
		if( location == null )
			return null;
		// build a pool of potential waypoints
		List waypoint_pool = new ArrayList();
		waypoint_pool.addAll( location.getPlanets() );
		waypoint_pool.addAll( location.getOrbitalStations() );
		waypoint_pool.addAll( location.getAsteroids() );
		if( waypoint_pool.isEmpty() )
			return null;
		// randomize pool order
		Collections.shuffle( waypoint_pool );
		// randomly choose a route size
		float min = waypoints_per_system_minimum;
		float max = waypoints_per_system_maximum;
		int route_size = (int)Math.round((float)(Math.random())*(max - min) + min);
		// create an appropriately sized slice of the shuffled pool as the route
		List route_list = waypoint_pool.subList( 0, (route_size - 1) );
		// convert to array
		SectorEntityToken[] route = new SectorEntityToken[route_list.size()];
		for( int i = 0; i < route.length; ++i )
			route[i] = ((SectorEntityToken)route_list.get( i ));
		// done
		return route;
	}
	
	private CampaignFleetAPI create_leader_fleet()
	{
		return sector.createFleet( faction_id, leader_fleet_id );
	}
	
	private CampaignFleetAPI[] create_escort_fleets()
	{
		CampaignFleetAPI[] escort_fleets = new CampaignFleetAPI[escort_fleet_count];
		for( int i = 0; i < escort_fleets.length; ++i )
		{
			String fleet_id = weighted_string_pick( 
				escort_fleet_composition_pool,
				escort_fleet_composition_weights );
			escort_fleets[i] = sector.createFleet( faction_id, fleet_id );
		}
		return escort_fleets;
	}
	
	private String weighted_string_pick( String[] pool, float[] weights )
	{
		int len = Math.min( pool.length, weights.length );
		float sum = 0f;
		for( int i = 0; i < len; ++i )
		{
			sum += weights[i];
		}
		float marker = (float)(Math.random())*sum; // pick
		sum = 0f;
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
	
	private void update_escort_fleets()
	{
		escort_positioner.update_escort_fleet_positions( leader_fleet, escort_fleets );
	}
	
	private boolean check_any_fleets_hyperspace_jumping()
	{
		if( leader_fleet.isInHyperspaceTransition() )
			return true;
		for( int i = 0; i < escort_fleets.length; ++i )
			if( escort_fleets[i].isInHyperspaceTransition() )
				return true;
		return false;
	}
	
	private void check_leader_still_alive()
	{
		if( leader_fleet == null || !leader_fleet.isAlive() )
		{
			scatter_remaining_escort_fleets();
			leader_fleet = null;
			escort_fleets = null;
			change_state( NON_EXISTENT );
		}
	}
	
	private void scatter_remaining_escort_fleets()
	{
		for( int i = 0; i < escort_fleets.length; ++i )
		{
			CampaignFleetAPI _escF = escort_fleets[i];
			if( _escF.isAlive() )
			{
				_escF.clearAssignments();
				// kill everyone aaghhhh!
				_escF.addAssignment( 
					FleetAssignment.RAID_SYSTEM,
					_escF.getContainingLocation().createToken( 0, 0 ), 
					Float.MAX_VALUE );
			}
		}
	}
	
	private void leader_return_home()
	{
		// could be very, very far away indeed; who knows if they'll even make it back?
		leader_fleet.addAssignment(
			FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, 
			spawn_location, 
			Float.MAX_VALUE );
	}
	
	private Vector2f _t1L;
	private Vector2f _t2L;
	private float _dx;
	private float _dy;
	////
	private float get_distance( SectorEntityToken t1, SectorEntityToken t2 )
	{
		if( t1 == null || t2 == null )
			return Float.MAX_VALUE;
		_t1L = t1.getLocation();
		_t2L = t2.getLocation();
		_dx = _t1L.x - _t2L.x;
		_dy = _t1L.y - _t2L.y;
		return (float)Math.sqrt( _dx*_dx + _dy*_dy );
	}

	// API methods must be defined
	public boolean isDone()
	{
		return false; // never done
	}

	public boolean runWhilePaused()
	{
		return false; // do not do this
	}
	
}

