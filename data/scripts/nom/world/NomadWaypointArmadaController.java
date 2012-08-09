package data.scripts.nom.world;

import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
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
	private CampaignClockAPI clock;

	// basic behavior options
	private int escort_fleet_count;
	private String[] escort_fleet_composition_pool;
	private float[] escort_fleet_composition_weights;
	private int waypoints_per_trip_minimum;
	private int waypoints_per_trip_maximum;
	private int waypoint_idle_time_days;
	private int out_of_sector_time_days;
	
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
	private LocationAPI[] current_route = null;
	// MOVING: next waypoint
	// WAITING: current waypoint
	private int current_route_waypoint_index = -1;
	
	// Constructor also initializes the spawning system and begins spawning fleets
	//  Spawning is immediate and automatic
	public NomadWaypointArmadaController( 
		SectorAPI sector, 
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
		this.clock = sector.getClock();
		this.escort_fleet_count = escort_fleet_count;
		this.escort_fleet_composition_pool = escort_fleet_composition_pool;
		this.escort_fleet_composition_weights = escort_fleet_composition_weights;
		this.waypoints_per_trip_minimum = waypoint_per_trip_minimum;
		this.waypoints_per_trip_maximum = waypoint_per_trip_maximum;
		this.waypoint_idle_time_days = waypoint_idle_time_days;
		this.out_of_sector_time_days = out_of_sector_time_days;
	}
	
	private LocationAPI[] build_fresh_route()
	{
		// build a pool of potential waypoints
		List waypoint_pool = new ArrayList();
		StarSystemAPI star_system = sector.getStarSystem("Corvus");
		waypoint_pool.addAll( star_system.getPlanets() );
		waypoint_pool.addAll( star_system.getOrbitalStations() );
		// choose randomly
		Collections.shuffle( waypoint_pool );
		// choose a route size
		double min = this.waypoints_per_trip_minimum;
		double max = this.waypoints_per_trip_maximum;
		double range = max - min;
		int route_size = (int)Math.round(Math.random()*range + min);
		// return an appropriately sized slice of the shuffled pool as the route
		return (LocationAPI[])
			(waypoint_pool.subList( 0, (route_size - 1) ).toArray());
	}

	
	@Override
	public void advance( SectorAPI sector, LocationAPI location )
	{
		switch( this.state )
		{ 
			////////////////////////////////////////
			case OUT_OF_SECTOR:
				// check if enough time has passed to spawn in next armada!
				if( this.first_run /* || this.out_of_sector_time_days */ )
				{
					// initialize a route
					this.current_route = build_fresh_route();
					this.current_route_waypoint_index = 0;
				}
				break;
			////////////////////////////////////////
			case IN_TRANSIT:
				// check distance from fleet leader to waypoint
				LocationAPI destination_waypoint = 
					this.current_route[this.current_route_waypoint_index];
				//float distance = get_distance(  )
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
		}
	}
	
	private void remove_all_fleets()
	{
		
	}
	
}

