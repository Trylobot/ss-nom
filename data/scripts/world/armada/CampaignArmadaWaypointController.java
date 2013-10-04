package data.scripts.world.armada;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import data.scripts._;
import data.scripts.world.armada.api.CampaignArmadaAPI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CampaignArmadaWaypointController implements Script
{
	private SectorAPI sector;
	
	private CampaignArmadaAPI armada;
	private Queue waypoints = new LinkedList();
	private boolean enabled = true;
	private int min_trip_length;
	private int max_trip_length;
	
	private boolean stalled = false; // set when no waypoints could be found anywhere (un-bloody-likely)
	
	public CampaignArmadaWaypointController(
		SectorAPI sector,
		CampaignArmadaAPI armada,
		int min_trip_length,
		int max_trip_length )
	{
		this.sector = sector;
		this.armada = armada;
		this.min_trip_length = min_trip_length;
		this.max_trip_length = max_trip_length;
	}
	
	public void run() // used for fleet assignment oncomplete
	{
		CampaignFleetAPI fleet = armada.getLeaderFleet();
		if( fleet == null )
		{
			// leader fleet is gone, armada is no longer tracking one (waiting to spawn, prolly)
			waypoints.clear();
			stalled = true;
			return;
		}
		if( waypoints.isEmpty() )
			generate_waypoints();
		CampaignArmadaWaypoint waypoint = (CampaignArmadaWaypoint) waypoints.poll();
		if( waypoint == null )
		{
			stalled = true;
			return;
		}
		fleet.addAssignment(
			waypoint.fleet_assignment,
			waypoint.target,
			waypoint.duration_in_days,
			this );
	}
	
	private void generate_waypoints()
	{
		// decide on maximum trip length (number of waypoints in a system)
		int trip_length = (int)(min_trip_length + Math.random() * (max_trip_length - min_trip_length + 1));
		// pick a system other than the current
		List systems = sector.getStarSystems();
		Collections.shuffle( systems );
		StarSystemAPI system = (StarSystemAPI)systems.get( 0 );
		// build a pool of potential waypoints from the entities in the chosen system
		List waypoint_pool = new ArrayList();
		waypoint_pool.add( system.getStar() );
		waypoint_pool.addAll( system.getPlanets() );
		waypoint_pool.addAll( system.getOrbitalStations() );
		waypoint_pool.addAll( system.getAsteroids() );
		if( waypoint_pool.isEmpty() )
		{
			_.L("could not find any stars, planets, stations or asteroids in StarSystemAPI "+system.getName());
			return;
		}
		// randomize pool order
		Collections.shuffle( waypoint_pool );
		for( Iterator i = waypoint_pool.iterator(); i.hasNext(); )
		{
			SectorEntityToken entity_token = (SectorEntityToken)i.next();
			if( entity_token == null )
				continue;
			waypoints.add( new CampaignArmadaWaypoint(
				FleetAssignment.GO_TO_LOCATION,
				entity_token,
			    Float.MAX_VALUE ));
		}
	}

	public class CampaignArmadaWaypoint
	{
		public FleetAssignment fleet_assignment;
		public SectorEntityToken target;
		public float duration_in_days;
		
		public CampaignArmadaWaypoint(
			FleetAssignment fleet_assignment,
			SectorEntityToken target,
			float duration_in_days )
		{
			this.fleet_assignment = fleet_assignment;
			this.target = target;
			this.duration_in_days = duration_in_days;
		}
	}
}
