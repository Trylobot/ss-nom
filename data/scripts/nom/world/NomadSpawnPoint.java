package data.scripts.nom.world;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;

public class NomadSpawnPoint extends BaseSpawnPoint {

	public NomadSpawnPoint(SectorAPI sector, LocationAPI location, 
							float daysInterval, int maxFleets, SectorEntityToken anchor) {
		super(sector, location, daysInterval, maxFleets, anchor);
	}

	@Override
	protected CampaignFleetAPI spawnFleet() {
		
		String type = null;
		float r = (float) Math.random();
		if        (r > 0.75f) {
			type = "scout";          // 25%
		} else if (r > 0.45f) {
			type = "longRangeScout"; // 30%
		} else if (r > 0.20f) { 
			type = "battleGroup";    // 25%
		} else {
			type = "royalGuard";     // 20%
		}
		
		CampaignFleetAPI fleet = getSector().createFleet("nomads", type);
		getLocation().spawnFleet(getAnchor(), 0, 0, fleet);
		fleet.addAssignment(FleetAssignment.RAID_SYSTEM, null, 10);
		
		return fleet;
	}

}
