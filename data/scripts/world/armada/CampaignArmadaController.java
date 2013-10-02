package data.scripts.world.armada;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import data.scripts.world.armada.api.CampaignArmadaAPI;
import data.scripts.world.armada.api.CampaignArmadaEscortFleetPositionerAPI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;


@SuppressWarnings("unchecked")
public class CampaignArmadaController implements EveryFrameScript, CampaignArmadaAPI
{
	// public constants for arguments
	private final static float STAR_SYSTEM_RADIUS = 15000f; // TODO: remove, spawn explicitly near a specified entity
	
	// current star system
	private SectorAPI sector;
	private StarSystemAPI spawn_system;
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
	private final static int NON_EXISTENT                = 10;
	private final static int IN_TRANSIT                  = 20;
	private final static int IDLE_AT_WAYPOINT            = 30;
	private final static int PERFORMING_HYPERSPACE_JUMP  = 40;
	// 
	private int state = NON_EXISTENT;
	
	// used for measuring idle time; defaults to very low value so armada will spawn immediately
	private long last_state_change_timestamp = Long.MIN_VALUE;
	
	private StarSystemAPI current_system = null;
	// currently selected local-space (in-system) waypoints composing a small planned trip for the leader fleet
	private SectorEntityToken[] current_route = null;
	// IN_TRANSIT: next waypoint
	// IDLE_AT_WAYPOINT: current waypoint
	private int current_route_waypoint_index = -1;
	
	// currently selected hyperspace bouy; used when current_route is empty
	private SectorEntityToken current_jump_point = null;
	
	// Constructor also initializes the spawning system and begins spawning fleets
	//  Spawning is immediate and automatic
    //  escort pool and weights assumed to be non-null, non-empty and of equal length
	public CampaignArmadaController( 
		String faction_id,
		String leader_fleet_id,
		SectorAPI sector,
		StarSystemAPI spawn_system,
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
		this.spawn_system = spawn_system;
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
		
		this.clock = sector.getClock();
	}
	
	// API methods
	public CampaignFleetAPI getLeaderFleet() { return leader_fleet; }
	public CampaignFleetAPI[] getEscortFleets() { return escort_fleets; }
	
	public CampaignArmadaEscortFleetPositionerAPI getEscortFleetPositioner() { return escort_positioner; }
	
	
	@Override
	public void advance( float amount )
	{
		switch( state )
		{ 
			////////////////////////////////////////
			case NON_EXISTENT:
				// check if enough time has passed to spawn in next armada!
				if( clock.getElapsedDaysSince( last_state_change_timestamp )
					>= dead_time_days )
				{
					// create leader fleet
					leader_fleet = create_leader_fleet();
					spawn_system.spawnFleet( 
						spawn_location, 0, 0, leader_fleet );
					current_system = spawn_system;
					// create escort fleets
					escort_fleets = create_escort_fleets();
					for( int i = 0; i < escort_fleets.length; ++i )
					{
						spawn_system.spawnFleet(
							spawn_location, 0, 0, escort_fleets[i] );
					}
					// initialize controller state
					change_state( IN_TRANSIT );
					advance_waypoint_index();
				}
				break;
			
			////////////////////////////////////////
			case IN_TRANSIT:
				// check if leader died; if so, scatter fleet
				check_leader_still_alive();
				// check distance from fleet leader to waypoint
				if( get_distance( leader_fleet, current_route[current_route_waypoint_index] )
					<= waypoint_achieved_radius )
				{
					change_state( IDLE_AT_WAYPOINT );
				}
				// keep escort fleets in formation
				update_escort_fleets();
				break;
			
			////////////////////////////////////////
			case IDLE_AT_WAYPOINT:
				// check if leader died; if so, scatter fleet
				check_leader_still_alive();
				// check what type of waypoint it is
				
				// IF CURRENT WAYPOINT IS REGULAR OL' WAYPOINT
					// check if idle time has elapsed
					if( clock.getElapsedDaysSince( last_state_change_timestamp ) 
						>= waypoint_idle_time_days )
					{
						change_state( IN_TRANSIT );
						advance_waypoint_index();
					}
				
				// ELSE IF CURRENT WAYPOINT IS HYPERSPACE BUOY
					/*
					 * The "jumpLocation" parameter does *not* have to be a jump point, it could just be a token
					 * at the location you'd like the jump to take place visually. The fleet will travel to that
					 * location and then jump to the destination. It should also handle it correctly
					 * (and actually change the current location mid-jump) if it's the player's fleet.
					 */
					// jump to a random destination belonging to the jump buoy
					//sector.doHyperspaceTransition( leader_fleet, current_hyperspace_waypoint, current_hyperspace_waypoint.get );
					// change state to jumping
					//change_state( HYPERSPACE_PERFORMING_JUMP );
					
				// keep escort fleets in formation
				update_escort_fleets();
				break;
			
			////////////////////////////////////////
			case PERFORMING_HYPERSPACE_JUMP:
				// check if leader died; if so, scatter fleet
				check_leader_still_alive();
				// check if anim still playing for any fleet
				
				// if anim still playing, nothing to do
				// else we just transitioned between normal space <--> hyperspace
					// if new location is hyperspace, choose a buoy
					current_jump_point = choose_jump_point( leader_fleet.getContainingLocation() );
				
					// else build a new route

					//// initialize a route
					//current_route = choose_system_waypoint_route( system );
					//current_route_waypoint_index = 0;
					
				
				// keep escort fleets in formation
				update_escort_fleets();
				break;
		}
	}
	
	private void change_state( int new_state )
	{
		state = new_state;
		last_state_change_timestamp = clock.getTimestamp();
	}
	
	private void advance_waypoint_index()
	{
		++current_route_waypoint_index;
		// check if last system waypoint reached
		
		if( current_route_waypoint_index >= current_route.length )
		{
			change_state( IN_TRANSIT );
			
		}
		else // not last waypoint, yet
		{
			leader_fleet.clearAssignments();
			
			leader_fleet.addAssignment(
				FleetAssignment.GO_TO_LOCATION, 
				current_route[current_route_waypoint_index],
				10000 );
		}
	}
	
	private SectorEntityToken choose_jump_point( LocationAPI location )
	{
		return null;
	}
	
	private SectorEntityToken[] choose_system_waypoint_route( LocationAPI location )
	{
		// build a pool of potential waypoints
		List waypoint_pool = new ArrayList();
		waypoint_pool.addAll( location.getPlanets() );
		waypoint_pool.addAll( location.getOrbitalStations() );
		// choose randomly
		Collections.shuffle( waypoint_pool );
		// choose a route size
		float min = waypoints_per_system_minimum;
		float max = waypoints_per_system_maximum;
		float range = max - min;
		int route_size = (int)Math.round((float)(Math.random())*range + min);
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
	
	
	// persistent allocations for frequently-called function
	private Vector2f _t1L;
	private Vector2f _t2L;
	private float _dx;
	private float _dy;
	////
	private float get_distance( SectorEntityToken t1, SectorEntityToken t2 )
	{
		_t1L = t1.getLocation();
		_t2L = t2.getLocation();
		_dx = _t1L.x - _t2L.x;
		_dy = _t1L.y - _t2L.y;
		return (float)Math.sqrt( _dx*_dx + _dy*_dy );
	}

	private void update_escort_fleets()
	{
		escort_positioner.update_escort_fleet_positions( leader_fleet, escort_fleets );
	}
	
	private void check_leader_still_alive()
	{
		if( leader_fleet == null || !leader_fleet.isAlive() )
		{
			scatter_remaining_escort_fleets();
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
				_escF.addAssignment(
					FleetAssignment.RAID_SYSTEM, spawn_system.getStar(), 1000 );
			}
		}
	}
	
	private void remove_all_fleets()
	{
		// despawn fleets
		leader_fleet.addAssignment(
			FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, 
			current_route[current_route.length - 1], 
			1000 );
	}
	

	public boolean isDone()
	{
		return false; // never done
	}

	public boolean runWhilePaused()
	{
		return false; // do not do this
	}
	
}

