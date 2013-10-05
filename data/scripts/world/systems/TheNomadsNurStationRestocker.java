package data.scripts.world.systems;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import data.scripts._;
import java.util.Iterator;

public class TheNomadsNurStationRestocker implements EveryFrameScript
{
	private float tick = 0f;
	private final float SCRIPT_UPDATE_WAIT_MIN_SEC = 1.0f;
	
	private CampaignClockAPI clock;
	//private float seconds_per_day;
	
	private String[]          restock_ship_variant_or_wing_ids;
	private FleetMemberType[] restock_ship_types;
	private int[]             restock_ship_count_cap;
	private float[]           restock_ship_wait_days;
	private SectorEntityToken orbital_station;
	
	private int               count;
	private long[]            restock_timestamps;
	

	public TheNomadsNurStationRestocker(
		String[]          restock_ship_variant_or_wing_ids,
		FleetMemberType[] restock_ship_types,
		int[]             restock_ship_count_cap,
		float[]           restock_ship_wait_days,
		SectorEntityToken orbital_station )
	{
		this.restock_ship_variant_or_wing_ids = restock_ship_variant_or_wing_ids;
		this.restock_ship_types = restock_ship_types;
		this.restock_ship_count_cap = restock_ship_count_cap;
		this.restock_ship_wait_days = restock_ship_wait_days;
		this.orbital_station = orbital_station;
		
		count = restock_ship_variant_or_wing_ids.length;
		restock_timestamps = new long[count];
		
		clock = Global.getSector().getClock();
		for( int i = 0; i < count; ++i )
			restock_timestamps[i] = clock.getTimestamp();
	}
	
	public void advance( float amount )
	{
		tick += amount;
		if( tick < SCRIPT_UPDATE_WAIT_MIN_SEC )
			return;
		tick -= SCRIPT_UPDATE_WAIT_MIN_SEC;
		
		for( int i = 0; i < count; ++i )
		{
			if( clock.getElapsedDaysSince( restock_timestamps[i] ) >= restock_ship_wait_days[i] )
			{
				restock_timestamps[i] = clock.getTimestamp();
				int stock = count_ship_stock( orbital_station, restock_ship_variant_or_wing_ids[i] );
				if( stock < restock_ship_count_cap[i] )
				{
					// add 1 ship
					orbital_station.getCargo().getMothballedShips().addFleetMember(
					  Global.getFactory().createFleetMember(
						restock_ship_types[i], restock_ship_variant_or_wing_ids[i] ));
					_.L("  ADDED mothballed ship "+restock_ship_variant_or_wing_ids[i]+" to station cargo");
				}
			}
		}
	}
	
	private int count_ship_stock( SectorEntityToken station, String variant_or_wing_id )
	{
		int stock = 0;
		FleetDataAPI station_ships = station.getCargo().getMothballedShips();
		for( Iterator i = station_ships.getMembersInPriorityOrder().iterator(); i.hasNext(); )
		{
			FleetMemberAPI ship = (FleetMemberAPI)i.next();
			if( variant_or_wing_id.equals(ship.getVariant().getHullVariantId()) )
				++stock;
		}
		return stock;
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
