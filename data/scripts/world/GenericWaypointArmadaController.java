package data.scripts.world;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.html.HTMLDocument;
import org.lwjgl.util.vector.Vector2f;

// TODO: refactor fleet formation positioner to use an interface, and a class for each escort type, allowing external implementations to be passed
//   "ORBIT"  params: orbit radius, orbit direction [CCW/CW], orbit period (days)
//   "COLUMN" params: separation distance, rank width number (width constant, files grow with number of escort fleets)
//   "LINE"   params: separation distance, number of files (number of files constant, rank width grows with number of escort fleets)
//   "SQUARE" params: separation distance
//   "WEDGE"  params: separation distance, offset angle
//   "ECHELON"params: separation distance, offset angle, variant [right/left]
//   "VEE"    params: separation distance, offset angle

// TODO: function for each state, or implement real state machine (seems like overkill tho)

@SuppressWarnings("unchecked")
public class GenericWaypointArmadaController implements EveryFrameScript
{
	// basic constants
	private final static float TWO_PI = (float)(2.0f*Math.PI);

	// public constants for arguments
	final public static int ESCORT_ORBIT = 100; // TODO: configurable
	private final static float WAYPOINT_ACHIEVED_DISTANCE = 10.0f; // TODO: configurable
	private final static float STAR_SYSTEM_RADIUS = 15000f; // TODO: remove, spawn explicitly near a specified entity
	
	// current star system
	private SectorAPI sector;
	private StarSystemAPI system;

	// basic behavior options
	private String faction_id;
	private String leader_fleet_id;
	private int escort_fleet_count;
	private String[] escort_fleet_composition_pool;
	private float[] escort_fleet_composition_weights;
	private int escort_formation;
	private float escort_formation_separation_distance;
	private int waypoints_per_system_minimum;
	private int waypoints_per_system_maximum;
	private int waypoint_idle_time_days;
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
	private final static int LOCALSPACE_IN_TRANSIT       = 20;
	private final static int LOCALSPACE_IDLE_AT_WAYPOINT = 30;
	private final static int HYPERSPACE_IN_TRANSIT       = 40;
	private final static int HYPERSPACE_PERFORMING_JUMP  = 50;	
	// 
	private int state = NON_EXISTENT;
	
	// used for measuring idle time; defaults to very low value so armada will spawn immediately
	private long last_state_change_timestamp = Long.MIN_VALUE;
	private long last_resource_distribution_check_timestamp = Long.MIN_VALUE;
	
	// hyperspace destination
	private SectorEntityToken current_hyperspace_waypoint = null;
	
	// currently selected local-space (in-system) waypoints composing a small planned trip for the leader fleet
	private SectorEntityToken[] current_route = null;
	// *_IN_TRANSIT: next waypoint
	// *_IDLE_*: current waypoint
	private int current_route_waypoint_index = -1;
	
	// Constructor also initializes the spawning system and begins spawning fleets
	//  Spawning is immediate and automatic
	public GenericWaypointArmadaController( 
		String faction_id,
		String leader_fleet_id,
		SectorAPI sector,
		StarSystemAPI system,
		int escort_fleet_count,
		String[] escort_fleet_composition_pool,
		float[] escort_fleet_composition_weights,
		int escort_formation,
		float escort_formation_separation_distance,
		int waypoint_per_trip_minimum,
		int waypoint_per_trip_maximum,
		int waypoint_idle_time_days,
		int dead_time_days,
		boolean enable_auto_resource_redistribution )
	{
		// setup behaviors; these are not modified by the controller
		this.faction_id = faction_id;
		this.leader_fleet_id = leader_fleet_id;
		this.sector = sector;
		this.system = system;
		this.escort_fleet_count = escort_fleet_count;
		// pool and weights assumed to be non-null, non-empty and of equal length
		this.escort_fleet_composition_pool = escort_fleet_composition_pool;
		this.escort_fleet_composition_weights = escort_fleet_composition_weights;
		this.escort_formation = escort_formation;
		this.escort_formation_separation_distance = escort_formation_separation_distance;
		this.waypoints_per_system_minimum = waypoint_per_trip_minimum;
		this.waypoints_per_system_maximum = waypoint_per_trip_maximum;
		this.waypoint_idle_time_days = waypoint_idle_time_days;
		this.dead_time_days = dead_time_days;
		
		this.enable_auto_resource_redistribution = enable_auto_resource_redistribution;
		this.clock = sector.getClock();
	}
	
	// getters
	public CampaignFleetAPI getLeaderFleet() { return leader_fleet; }
	public CampaignFleetAPI[] getEscortFleets() { return escort_fleets; }
	
	
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
					// initialize a route
					current_route = build_fresh_route();
					current_route_waypoint_index = 0;
					// create leader fleet
					leader_fleet = create_leader_fleet();
					system.spawnFleet( 
						current_route[0], 0, 0, leader_fleet );
					// create escort fleets
					escort_fleets = create_escort_fleets();
					for( int i = 0; i < escort_fleets.length; ++i )
					{
						system.spawnFleet(
							current_route[0], 0, 0, escort_fleets[i] );
					}
					// controller state
					change_state( LOCALSPACE_IN_TRANSIT );
					advance_waypoint_index();
				}
				break;
			
			////////////////////////////////////////
			case LOCALSPACE_IN_TRANSIT:
				// check if leader died; if so, scatter fleet
				check_leader_still_alive();
				// check distance from fleet leader to waypoint
				if( get_distance( leader_fleet, current_route[current_route_waypoint_index] )
					<= WAYPOINT_ACHIEVED_DISTANCE )
				{
					change_state( LOCALSPACE_IDLE_AT_WAYPOINT );
				}
				// keep escort fleets moving in formation
				update_escort_fleets();
				redistribute_resources();
				break;
			
			////////////////////////////////////////
			case LOCALSPACE_IDLE_AT_WAYPOINT:
				// check if leader died; if so, scatter fleet
				check_leader_still_alive();
				// check if idle time has elapsed
				if( clock.getElapsedDaysSince( last_state_change_timestamp ) 
					>= waypoint_idle_time_days )
				{
					change_state( LOCALSPACE_IN_TRANSIT );
					advance_waypoint_index();
				}
				// keep escort fleets in formation while idle
				update_escort_fleets();
				redistribute_resources();
				break;
			
			////////////////////////////////////////
			case HYPERSPACE_IN_TRANSIT:
				// check if leader died; if so, scatter fleet
				check_leader_still_alive();
				// check distance from fleet leader to waypoint
				if( get_distance( leader_fleet, current_hyperspace_waypoint )
					<= WAYPOINT_ACHIEVED_DISTANCE )
				{
					// jump
					
					// change state to jumping
					change_state( HYPERSPACE_PERFORMING_JUMP );
				}
				update_escort_fleets();
				redistribute_resources();
				
				break;
			
			////////////////////////////////////////
			case HYPERSPACE_PERFORMING_JUMP:
				
				break;
		}
	}
	
	private SectorEntityToken[] build_fresh_route()
	{
		// build a pool of potential waypoints
		List waypoint_pool = new ArrayList();
		waypoint_pool.addAll( system.getPlanets() );
		waypoint_pool.addAll( system.getOrbitalStations() );
		// choose randomly
		Collections.shuffle( waypoint_pool );
		// choose a route size
		float min = waypoints_per_system_minimum;
		float max = waypoints_per_system_maximum;
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
		SectorEntityToken start = system.createToken( spawn_loc.x, spawn_loc.y );
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
			change_state( NON_EXISTENT );
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
	//private Vector2f _eP = new Vector2f(); // escort fleet position
	private static final float _TS_FACTOR = 100000000.0f; // arbitrary (do not modify)
	private float _o_sK; // orbit speed factor (could be parameterized)
	private float _o_s; // orbit speed
	////
	private void update_escort_fleets()
	{
		// get position, speed, and direction of leader fleet.
		_LP = leader_fleet.getLocation();
		_LV = leader_fleet.getVelocity();

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
						_eA = i*(TWO_PI/(float)escort_fleets.length) + _phA;
						_escF.setLocation(
						  (float)(_LP.x + _r*Math.cos( _eA )),
						  (float)(_LP.y + _r*Math.sin( _eA )) );
						_escF.getVelocity().set( _LV ); // for proper hyperspace fuel burn
					}
				}
				break;
		}
	}
	
	private void check_leader_still_alive()
	{
		if( !leader_fleet.isAlive() )
		{
			scatter_remaining_escort_fleets();
			change_state( NON_EXISTENT );
		}
	}
	
	private int count_alive_fleets()
	{
		int count = 0;
		for( int i = 0; i < escort_fleets.length; ++i )
			if( escort_fleets[i].isAlive() )
				++count;
		if( leader_fleet.isAlive() )
			++count;
		return count;
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
					FleetAssignment.RAID_SYSTEM, system.getStar(), 1000 );
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
	
	///////////////////////////////////////////////////////////////////////////////
	// TODO: should auto resource redistribution be moved to its own class? maybe

	private boolean enable_auto_resource_redistribution;
	
	private float fleet_risk_threshold_days_worth_of_supplies   = 3.0f; // 3 days at fleet's current usage (whatever it happens to be) // TODO: average actual usage over past N days?
	private float fleet_risk_threshold_extra_crew_percentage    = 0.10f; // skeleton crew requirement, plus 10%
	private float fleet_risk_threshold_lightyears_worth_of_fuel = 5.0f; // 5 light-years worth of fuel at fleet's current fuel consumption rate
	private float fleet_abundance_threshold_days_worth_of_supplies   = 12.0f; // 12 days at fleet's current usage (whatever it happens to be)
	private float fleet_abundance_threshold_extra_crew_percentage    = 0.25f; // skeleton crew requirement, plus 25%
	private float fleet_abundance_threshold_lightyears_worth_of_fuel = 15.0f; // 15 light-years worth of fuel at fleet's current fuel consumption rate
	
	private List get_jeopardized_fleets()
	{
		ArrayList list = new ArrayList( escort_fleets.length );
		for( int i = 0; i < escort_fleets.length; ++i )
		{
			if( !escort_fleets[i].isAlive() )
				continue;
			if( calculate_needed_crew( escort_fleets[i] ) > 0
			||  calculate_needed_supplies( escort_fleets[i] ) > 0 )
			{
				list.add( escort_fleets[i] );
			}
			// TODO: other resources
		}
		if( leader_fleet.isAlive() 
		&&( calculate_needed_crew( leader_fleet ) > 0 
		||  calculate_needed_supplies( leader_fleet ) > 0 ))
		{
			list.add( leader_fleet );
		}
		return list;
	}
	
	private List get_generous_fleets()
	{
		ArrayList list = new ArrayList( escort_fleets.length );
		// for each fleet, that fleet is at risk if: ...
		for( int i = 0; i < escort_fleets.length; ++i )
		{
			if( !escort_fleets[i].isAlive() )
				continue;
			// if an escort fleet needs supplies
			if( calculate_noncritical_crew( escort_fleets[i] ) > 0 
			||  calculate_noncritical_supplies( escort_fleets[i] ) > 0 )
			{
				list.add( escort_fleets[i] );
			}
			// TODO: other resources
		}
		if( leader_fleet.isAlive()
		&&( calculate_noncritical_crew( leader_fleet ) > 0 
		||  calculate_noncritical_supplies( leader_fleet ) > 0 ))
		{
			list.add( leader_fleet );
		}
		return list;
	}
	
	
	private float calculate_needed_crew( CampaignFleetAPI fleet )
	{
		return ((fleet_risk_threshold_extra_crew_percentage * calculate_fleet_min_total_crew( fleet ))
		  - (float)fleet.getCargo().getTotalCrew() );
	}
	private float calculate_fleet_min_total_crew( CampaignFleetAPI fleet )
	{
		float count = 0.0f;
		List members = fleet.getFleetData().getMembersInPriorityOrder();
		for( Iterator i = members.iterator(); i.hasNext(); )
			count += ((FleetMemberAPI)i.next()).getMinCrew();
		return count;
	}
	
	private float calculate_needed_supplies( CampaignFleetAPI fleet )
	{
		return ((fleet_risk_threshold_days_worth_of_supplies * fleet.getTotalSupplyCostPerDay())
		  - fleet.getCargo().getSupplies());
	}
	
	private float calculate_needed_fuel( CampaignFleetAPI fleet )
	{
		return ((fleet_risk_threshold_days_worth_of_supplies * fleet.getTotalSupplyCostPerDay())
		  - fleet.getCargo().getSupplies());
	}
	
	
	private float calculate_noncritical_crew( CampaignFleetAPI fleet )
	{
		return ((float)fleet.getCargo().getTotalCrew()
		  - (fleet_risk_threshold_extra_crew_percentage * calculate_fleet_min_total_crew( fleet )));
	}
	
	private float calculate_noncritical_supplies( CampaignFleetAPI fleet )
	{
		return (fleet.getCargo().getSupplies()
		  - (fleet_abundance_threshold_days_worth_of_supplies * fleet.getTotalSupplyCostPerDay()));
	}
	
	private float calculate_noncritical_fuel( CampaignFleetAPI fleet )
	{
		return (fleet.getCargo().getSupplies()
		  - (fleet_abundance_threshold_days_worth_of_supplies * fleet.getTotalSupplyCostPerDay()));
	}
	
	private void redistribute_resources()
	{
		// toggle behavior
		if( !enable_auto_resource_redistribution )
			return;
		// do this once per day.
		if( clock.getElapsedDaysSince( last_resource_distribution_check_timestamp )
			<= 1.0f )
		{
			return;
		}
		last_resource_distribution_check_timestamp = clock.getTimestamp();
		
		// if the leader fleet is destroyed, abort
		if( !leader_fleet.isAlive() )
			return;
		// if less than one fleets remain, abort
		if( count_alive_fleets() < 1 )
			return; // no one to share with
		
		// search for fleets that are:
		// * at risk of an accident due to low supplies, or
		// * have undeployable ships due to low ship CR
		List jeopardized_fleets = get_jeopardized_fleets();
		if( jeopardized_fleets.size() <= 0 )
			return; // no one needs anything
		// then, calculate the needed resources to restore those fleets to desired levels.
		float needed_crew =     0.0f;
		float needed_supplies = 0.0f;
		float needed_fuel =     0.0f;
		for( int i = 0; i < jeopardized_fleets.size(); ++i )
		{
			CampaignFleetAPI fleet = (CampaignFleetAPI)jeopardized_fleets.get( i );
			needed_crew +=     calculate_needed_crew( fleet );
			needed_supplies += calculate_needed_supplies( fleet );
			needed_fuel +=     calculate_needed_fuel( fleet );
		}
		
		List generous_fleets = get_generous_fleets();
		if( generous_fleets.size() <= 0 )
			return; // no one has anything extra available
		// then, check if the required resources exist within all of the non-at-risk fleets as a group
		float available_crew =     0.0f;
		float available_supplies = 0.0f;
		float available_fuel =     0.0f;
		for( int i = 0; i < generous_fleets.size(); ++i )
		{
			CampaignFleetAPI fleet = (CampaignFleetAPI)generous_fleets.get( i );
			available_crew +=     calculate_noncritical_crew( fleet );
			available_supplies += calculate_noncritical_supplies( fleet );
			available_fuel +=     calculate_noncritical_fuel( fleet );
		}
		
		// if they do, transfer resources.
		//   all source fleets contribute a number of resources. the actual number will vary such that donated percentages of total fleet resources are equal
		
		
		
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

