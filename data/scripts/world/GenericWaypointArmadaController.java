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
	
	final private static float WAYPOINT_ACHIEVED_DISTANCE = 0.5f;

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
		switch( this.state )
		{ 
			////////////////////////////////////////
			case OUT_OF_SECTOR:
				// check if enough time has passed to spawn in next armada!
				if( this.first_run 
				|| this.clock.getElapsedDaysSince( this.last_state_change_timestamp ) 
				   >= this.out_of_sector_time_days )
				{
					// initialize a route
					this.current_route = build_fresh_route();
					this.current_route_waypoint_index = 0;
					// create leader fleet
					this.leader_fleet = create_leader_fleet();
					this.star_system.spawnFleet( 
						this.current_route[0], 
						STAR_SYSTEM_RADIUS, STAR_SYSTEM_RADIUS, 
						leader_fleet );
					// create escort fleets
					this.escort_fleets = create_escort_fleets();
					for( int i = 0; i < this.escort_fleets.length; ++i )
					{
						this.star_system.spawnFleet(
							this.current_route[0], 
							STAR_SYSTEM_RADIUS, STAR_SYSTEM_RADIUS,
							this.escort_fleets[i] );
					}
					// controller state
					change_state( IN_TRANSIT );
					advance_waypoint_index();
				}
				break;
			////////////////////////////////////////
			case IN_TRANSIT:
				// check distance from fleet leader to waypoint
				SectorEntityToken destination_waypoint = 
					this.current_route[this.current_route_waypoint_index];
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
				// check if idle time has elapsed
				if( this.clock.getElapsedDaysSince( this.last_state_change_timestamp ) 
				    >= this.waypoint_idle_time_days )
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
		waypoint_pool.addAll( this.star_system.getPlanets() );
		waypoint_pool.addAll( this.star_system.getOrbitalStations() );
		// choose randomly
		Collections.shuffle( waypoint_pool );
		// choose a route size
		float min = this.waypoints_per_trip_minimum;
		float max = this.waypoints_per_trip_maximum;
		float range = max - min;
		int route_size = (int)Math.round((float)(Math.random())*range + min);
		// create an appropriately sized slice of the shuffled pool as the route
		List route_list = waypoint_pool.subList( 0, (route_size - 1) );
		// create and add entry/exit waypoint
		SectorEntityToken start = this.star_system.createToken( STAR_SYSTEM_RADIUS, STAR_SYSTEM_RADIUS );
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
		return this.sector.createFleet( this.faction_id, this.leader_fleet_id );
	}
	
	private CampaignFleetAPI[] create_escort_fleets()
	{
		CampaignFleetAPI[] escort_fleets = new CampaignFleetAPI[this.escort_fleet_count];
		for( int i = 0; i < escort_fleets.length; ++i )
		{
			String fleet_id = weighted_string_pick( 
				this.escort_fleet_composition_pool,
				this.escort_fleet_composition_weights );
			escort_fleets[i] = this.sector.createFleet( this.faction_id, fleet_id );
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
		// get position, speed, and direction of leader fleet.
		// predict position of leader in the near future.
		// calculate array of target positions based on formation and prediction.
		// assign orders to escort fleets.
	}
	
	private float get_distance( SectorEntityToken t1, SectorEntityToken t2 )
	{
		Vector2f v1 = t1.getLocation();
		Vector2f v2 = t2.getLocation();
		float dx = v2.x - v1.x;
		float dy = v2.y - v1.y;
		return (float)Math.sqrt( dx*dx + dy*dy );
	}

	private void change_state( int new_state )
	{
		this.state = new_state;
		this.last_state_change_timestamp = this.clock.getTimestamp();
	}
	
	private void advance_waypoint_index()
	{
		++this.current_route_waypoint_index;
		// check if last waypoint reached
		if( this.current_route_waypoint_index >= this.current_route.length )
		{
			change_state( OUT_OF_SECTOR );
			remove_all_fleets();
		}
		else // not last waypoint, yet
		{
			this.leader_fleet.addAssignment(
				FleetAssignment.GO_TO_LOCATION, 
				this.current_route[this.current_route_waypoint_index], 
				1000 );
		}
	}
	
	private void remove_all_fleets()
	{
		// despawn fleets
		this.leader_fleet.addAssignment(
			FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, 
			this.current_route[this.current_route.length - 1], 
			1000 );
	}
	
}

