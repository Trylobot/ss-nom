package data.scripts.nom.world;

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
public class NomadWaypointArmadaController implements SpawnPointPlugin
{
	// Useful constants and enums
	final private float WAYPOINT_ACHIEVED_DISTANCE = 10.0f;

	private enum ARMADA_STATE
	{
		OUT_OF_SECTOR,
		IN_TRANSIT,
		IDLE_AT_WAYPOINT
	};
	
	// current star system
	private SectorAPI sector;
	private LocationAPI location;

	// basic behavior options
	private int escort_fleet_count;
	private String[] escort_fleet_composition_pool;
	private float[] escort_fleet_composition_weights;
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
	private ARMADA_STATE state = ARMADA_STATE.OUT_OF_SECTOR;
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
	public NomadWaypointArmadaController( 
		SectorAPI sector,
		LocationAPI location,
		int escort_fleet_count,
		String[] escort_fleet_composition_pool,
		float[] escort_fleet_composition_weights,
		int waypoint_per_trip_minimum,
		int waypoint_per_trip_maximum,
		int waypoint_idle_time_days,
		int out_of_sector_time_days )
	{
		// setup behaviors; these are not modified by the controller
		this.sector = sector;
		this.location = location;
		this.escort_fleet_count = escort_fleet_count;
		this.escort_fleet_composition_pool = escort_fleet_composition_pool;
		this.escort_fleet_composition_weights = escort_fleet_composition_weights;
		this.waypoints_per_trip_minimum = waypoint_per_trip_minimum;
		this.waypoints_per_trip_maximum = waypoint_per_trip_maximum;
		this.waypoint_idle_time_days = waypoint_idle_time_days;
		this.out_of_sector_time_days = out_of_sector_time_days;

		this.star_system = sector.getStarSystem("Corvus");
		this.clock = sector.getClock();
		
		this.location.addSpawnPoint(this);
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
					// create fleets
					this.leader_fleet = create_leader_fleet();
					this.star_system.spawnFleet( 
						this.current_route[0], 0, 0, leader_fleet);
					// controller state
					change_state( ARMADA_STATE.IN_TRANSIT );
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
					change_state( ARMADA_STATE.IDLE_AT_WAYPOINT );
				}
				// keep escort fleets moving in formation
				
				break;
			////////////////////////////////////////
			case IDLE_AT_WAYPOINT:
				// check if idle time has elapsed
				if( this.clock.getElapsedDaysSince( this.last_state_change_timestamp ) 
				    >= this.waypoint_idle_time_days )
				{
					change_state( ARMADA_STATE.IN_TRANSIT );
					advance_waypoint_index();
				}
				// keep escort fleets in formation while idle
				
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
		List route = waypoint_pool.subList( 0, (route_size - 1) );
		// create and add entry/exit waypoint
		SectorEntityToken start = this.star_system.createToken( 0f, 0f );
		SectorEntityToken end = this.star_system.createToken( 0f, 0f );
		route.add( 0, start );
		route.add( end );
		return (SectorEntityToken[])route.toArray();
	}
	
	private CampaignFleetAPI create_leader_fleet()
	{
		return this.sector.createFleet("nomads", "colonyFleet");
	}
	
	private CampaignFleetAPI[] create_escort_fleets()
	{
		return null;
	}

	private float get_distance( SectorEntityToken t1, SectorEntityToken t2 )
	{
		Vector2f v1 = t1.getLocation();
		Vector2f v2 = t2.getLocation();
		float dx = v2.x - v1.x;
		float dy = v2.y - v1.y;
		return (float)Math.sqrt( dx*dx + dy*dy );
	}

	private void change_state( ARMADA_STATE new_state )
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
			change_state( ARMADA_STATE.OUT_OF_SECTOR );
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

