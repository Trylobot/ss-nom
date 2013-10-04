package data.hullmods.base;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.hullmods.BaseHullMod;
import data.scripts._;
import java.util.Iterator;

public abstract class BaseFleetEffectHullMod extends BaseHullMod
{
	private CampaignFleetAPI last_fleet_found = null;
	
	public CampaignFleetAPI findFleet( FleetMemberAPI member )
	{
		PersonAPI commander = member.getFleetCommander();
		if( last_fleet_found != null && last_fleet_found.getCommander() == commander )
			return last_fleet_found;
		// search all fleets for the commander
		for( Iterator star_system_i = Global.getSector().getStarSystems().iterator(); star_system_i.hasNext(); )
		{
			for( Iterator fleet_i = ((StarSystemAPI)star_system_i.next()).getFleets().iterator(); fleet_i.hasNext(); )
			{
				CampaignFleetAPI fleet = (CampaignFleetAPI)fleet_i.next();
				if( commander == fleet.getCommander() )
				{
					last_fleet_found = fleet;
					_.L("found fleet by commander");
					return fleet;
				}
			}
		}
		return null;
	}	
}
