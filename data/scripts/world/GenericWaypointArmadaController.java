package data.scripts.world;

import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpawnPointPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;

@SuppressWarnings("unchecked")
public class GenericWaypointArmadaController implements SpawnPointPlugin
{
	// public constants for arguments
	final public static int ESCORT_ORBIT = 100;
	
	// constants and enums
	final private static float STAR_SYSTEM_RADIUS = 15000f;
	final private static float TWO_PI = (float)(2.0f*Math.PI);
	
	final private static float WAYPOINT_ACHIEVED_DISTANCE = 10.0f;

	final private static int OUT_OF_SECTOR    = 10;
	final private static int IN_TRANSIT       = 20;
	final private static int IDLE_AT_WAYPOINT = 30;
	
	// current star system
	private SectorAPI sector;
	private LocationAPI location;

	// basic behavior options
	private String faction_id;
	private String leader_fleet_id;
	private int escort_fleet_count;
	private String[] escort_fleet_composition_pool;
	private float[] escort_fleet_composition_weights;
	private int escort_formation;
	private float escort_formation_separation_distance;
	private int waypoints_per_trip_minimum;
	private int waypoints_per_trip_maximum;
	private int waypoint_idle_time_days;
	private int out_of_sector_time_days;
	
	private StarSystemAPI star_system;
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

	// State of the controller
	private int state = OUT_OF_SECTOR;
	private boolean first_run = true;
	
	// used for measuring idle time
	private long last_state_change_timestamp = 0;
	
	// Currently selected waypoints composing a planned trip for the leader fleet
	private SectorEntityToken[] current_route = null;
	// MOVING: next waypoint
	// WAITING: current waypoint
	private int current_route_waypoint_index = -1;
	
	// Constructor also initializes the spawning system and begins spawning fleets
	//  Spawning is immediate and automatic
	public GenericWaypointArmadaController( 
		String faction_id,
		String leader_fleet_id,
		SectorAPI sector,
		LocationAPI location,
		int escort_fleet_count,
		String[] escort_fleet_composition_pool,
		float[] escort_fleet_composition_weights,
		int escort_formation,
		float escort_formation_separation_distance,
		int waypoint_per_trip_minimum,
		int waypoint_per_trip_maximum,
		int waypoint_idle_time_days,
		int out_of_sector_time_days )
	{
		// setup behaviors; these are not modified by the controller
		this.faction_id = faction_id;
		this.leader_fleet_id = leader_fleet_id;
		this.sector = sector;
		this.location = location;
		this.escort_fleet_count = escort_fleet_count;
		// pool and weights assumed to be non-null, non-empty and of equal length
		this.escort_fleet_composition_pool = escort_fleet_composition_pool;
		this.escort_fleet_composition_weights = escort_fleet_composition_weights;
		this.escort_formation = escort_formation;
		this.escort_formation_separation_distance = escort_formation_separation_distance;
		this.waypoints_per_trip_minimum = waypoint_per_trip_minimum;
		this.waypoints_per_trip_maximum = waypoint_per_trip_maximum;
		this.waypoint_idle_time_days = waypoint_idle_time_days;
		this.out_of_sector_time_days = out_of_sector_time_days;

		this.star_system = sector.getStarSystem("Corvus");
		this.clock = sector.getClock();
	}
	
	
	@Override
	public void advance( SectorAPI sector, LocationAPI location )
	{
		switch( state )
		{ 
			////////////////////////////////////////
			case OUT_OF_SECTOR:
				// check if enough time has passed to spawn in next armada!
				if( first_run 
				|| clock.getElapsedDaysSince( last_state_change_timestamp ) 
				   >= out_of_sector_time_days )
				{
					// initialize a route
					current_route = build_fresh_route();
					current_route_waypoint_index = 0;
					// create leader fleet
					leader_fleet = create_leader_fleet();
					star_system.spawnFleet( 
						current_route[0], 0, 0, leader_fleet );
					// create escort fleets
					escort_fleets = create_escort_fleets();
					for( int i = 0; i < escort_fleets.length; ++i )
					{
						star_system.spawnFleet(
							current_route[0], 0, 0, escort_fleets[i] );
					}
					// controller state
					change_state( IN_TRANSIT );
					advance_waypoint_index();
				}
				break;
			
			////////////////////////////////////////
			case IN_TRANSIT:
				// check if leader died; if so, scatter fleet
				check_leader_still_alive();
				// check distance from fleet leader to waypoint
				SectorEntityToken destination_waypoint = 
					current_route[current_route_waypoint_index];
				float distance = get_distance( leader_fleet, destination_waypoint );
				if( distance <= WAYPOINT_ACHIEVED_DISTANCE )
				{
					change_state( IDLE_AT_WAYPOINT );
				}
				// keep escort fleets moving in formation
				update_escort_fleets();
				break;
			
			////////////////////////////////////////
			case IDLE_AT_WAYPOINT:
				// check if leader died; if so, scatter fleet
				check_leader_still_alive();
				// check if idle time has elapsed
				if( clock.getElapsedDaysSince( last_state_change_timestamp ) 
				    >= waypoint_idle_time_days )
				{
					change_state( IN_TRANSIT );
					advance_waypoint_index();
				}
				// keep escort fleets in formation while idle
				update_escort_fleets();
				break;
		}
	}
	
	private SectorEntityToken[] build_fresh_route()
	{
		// build a pool of potential waypoints
		List waypoint_pool = new ArrayList();
		waypoint_pool.addAll( star_system.getPlanets() );
		waypoint_pool.addAll( star_system.getOrbitalStations() );
		// choose randomly
		Collections.shuffle( waypoint_pool );
		// choose a route size
		float min = waypoints_per_trip_minimum;
		float max = waypoints_per_trip_maximum;
		float range = max - min;
		int route_size = (int)Math.round((float)(Math.random())*range + min);
		// create an appropriately sized slice of the shuffled pool as the route
		List route_list = waypoint_pool.subList( 0, (route_size - 1) );
		// create and add entry/exit waypoint.
		// choose a point at random that lies on the perimeter of the square 
		//  depicting the 'border' of the sector.
		Vector2f spawn_loc = new Vector2f( 
			(float)(Math.random()*(2.0f*STAR_SYSTEM_RADIUS) - STAR_SYSTEM_RADIUS), 
			STAR_SYSTEM_RADIUS );
		if( Math.random() > 0.5 ) // 50% chance to flip from top to bottom
			spawn_loc.y *= -1;
		if( Math.random() > 0.5 ) // 50% chance to flip from left to right
		{
			float swap = spawn_loc.x;
			spawn_loc.x = spawn_loc.y;
			spawn_loc.y = swap;
		}
		SectorEntityToken start = star_system.createToken( spawn_loc.x, spawn_loc.y );
		SectorEntityToken end = start;
		route_list.add( 0, start );
		route_list.add( end );
		SectorEntityToken[] route = new SectorEntityToken[route_list.size()];
		for( int i = 0; i < route.length; ++i )
		{
			route[i] = (SectorEntityToken)route_list.get( i );
		}
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

	private void change_state( int new_state )
	{
		state = new_state;
		last_state_change_timestamp = clock.getTimestamp();
	}
	
	private void advance_waypoint_index()
	{
		++current_route_waypoint_index;
		// check if last waypoint reached
		if( current_route_waypoint_index >= current_route.length )
		{
			change_state( OUT_OF_SECTOR );
			remove_all_fleets();
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
	
	// persistent allocations for frequently-called function
	private CampaignFleetAPI _escF;
	private float _r; // radius
	private Vector2f _LP; // leader position
	private Vector2f _LV; // leader velocity
	private float _phA; // phase angle constant
	private float _eA; // escort fleet angle
	private Vector2f _eP = new Vector2f(); // escort fleet position
	private static final float _TS_FACTOR = 100000000.0f; // arbitrary (do not modify)
	private float _o_sK; // orbit speed factor (could be parameterized)
	private float _o_s; // orbit speed
	////
	private void update_escort_fleets()
	{
		// get position, speed, and direction of leader fleet.
		_LP = leader_fleet.getLocation();
		//_LV = leader_fleet.getVelocity();
		// predict position of leader in the near future.
		//   ... to do
		// calculate array of target positions based on formation and prediction.
		// assign orders to escort fleets.
		switch( escort_formation )
		{
			case( ESCORT_ORBIT ):
				// separation distance is the radius of the orbit.
				// escort fleets are evenly-spaced around the circumference of the orbit.
				// the orbit phase is locked to allow for slow fleets
				_o_sK = 6.00f;
				_o_s = _TS_FACTOR / _o_sK;
				_phA = (float)((clock.getTimestamp()%((long)(TWO_PI*_o_s)))/_o_s);
				
				_r = escort_formation_separation_distance;

				for( int i = 0; i < escort_fleets.length; ++i )
				{
					_escF = escort_fleets[i];
					if( _escF.isAlive() )
					{
						_escF.clearAssignments();

						_eA = i*(TWO_PI/(float)escort_fleets.length) + _phA;
						_eP.x = (float)(_LP.x + _r*Math.cos( _eA ));
						_eP.y = (float)(_LP.y + _r*Math.sin( _eA ));

						_escF.setLocation(_eP.x, _eP.y); // looks cooler.
						/*escort_fleets[i].addAssignment(
							FleetAssignment.DEFEND_LOCATION,
							star_system.createToken(_eP.x, _eP.y),
							10000 );*/
					}
				}
				break;
		}
	}
	
	private void check_leader_still_alive()
	{
		if( ! leader_fleet.isAlive() )
		{
			scatter_remaining_escort_fleets();
			change_state( OUT_OF_SECTOR );
		}
	}
	
	private void scatter_remaining_escort_fleets()
	{
		for( int i = 0; i < escort_fleets.length; ++i )
		{
			_escF = escort_fleets[i];
			if( _escF.isAlive() )
			{
				_escF.clearAssignments();
				_escF.addAssignment(
					FleetAssignment.RAID_SYSTEM, star_system.getStar(), 1000 );
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
	
}

