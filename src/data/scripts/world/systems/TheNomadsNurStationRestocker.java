package data.scripts.world.systems;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.loading.HullModSpecAPI;


public class TheNomadsNurStationRestocker implements EveryFrameScript
{
	private float tick = 0f;
	private final float SCRIPT_UPDATE_WAIT_MIN_SEC = 1.0f;
	
	private CampaignClockAPI clock;
	//private float seconds_per_day;
	
	private StockDescriptor[] restock;
	private SectorEntityToken orbital_station;
	
	private int               count;
	private long[]            restock_timestamps;
  
  public TheNomadsNurStationRestocker(
    StockDescriptor[] restock,
    SectorEntityToken orbital_station )
  {
    this.restock = restock;
    this.orbital_station = orbital_station;

    count = restock.length;
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
		
		SubmarketAPI open_market = orbital_station.getMarket().getSubmarket("open_market");
    //
    for( int i = 0; i < count; ++i )
		{
			if( clock.getElapsedDaysSince( restock_timestamps[i] ) >= restock[i].wait_days )
			{
				restock_timestamps[i] = clock.getTimestamp();
				int stock = count_stock( orbital_station, restock[i] );
				if( stock < restock[i].count_cap )
				{
					if (restock[i].type == StockDescriptor.SHIP)
          {
            open_market.getCargo().getMothballedShips().addFleetMember(
              Global.getFactory().createFleetMember(
                FleetMemberType.SHIP, restock[i].id) );
          }
          else if (restock[i].type == StockDescriptor.FIGHTER_LPC) {
            orbital_station.getMarket().getSubmarket("open_market").getCargo()
              .addFighters(restock[i].id, 1);
          }
          else if (restock[i].type == StockDescriptor.HULLMOD_SPEC) {
            orbital_station.getMarket().getSubmarket("open_market").getCargo()
              .addHullmods(restock[i].id, 1);
          }
				}
			}
		}
	}
	
	private int count_stock( SectorEntityToken station, StockDescriptor restock )
	{
		int stock = 0;
    //
    if (restock.type == StockDescriptor.SHIP) {
      SubmarketAPI open_market = station.getMarket().getSubmarket("open_market");
      for (FleetMemberAPI ship : open_market.getCargo().getMothballedShips().getMembersInPriorityOrder()) {
        if( restock.id.equals( ship.getSpecId() ))
          ++stock;
      }
    } else if (restock.type == StockDescriptor.FIGHTER_LPC) {
      stock += station.getMarket().getSubmarket("open_market").getCargo().getNumFighters( restock.id );
    } else if (restock.type == StockDescriptor.HULLMOD_SPEC) {
      for (CargoStackAPI cargo_stack : station.getMarket().getSubmarket("open_market").getCargo().getStacksCopy()) {
        HullModSpecAPI hullmod_spec = cargo_stack.getHullModSpecIfHullMod();
        if (hullmod_spec != null && hullmod_spec.getId() == restock.id) {
          stock += cargo_stack.getSize();
        }
      }
    }
    //
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
