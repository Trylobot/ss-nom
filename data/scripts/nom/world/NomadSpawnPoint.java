package data.scripts.nom.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.*;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.fleet.*;

import data.scripts.world.*;

public class NomadSpawnPoint extends BaseSpawnPoint {

	public NomadSpawnPoint(SectorAPI sector, LocationAPI location, 
							float daysInterval, int maxFleets, SectorEntityToken anchor) {
		super(sector, location, daysInterval, maxFleets, anchor);
	}

	@Override
	protected CampaignFleetAPI spawnFleet() {
		
		String type = null;
		float r = (float) Math.random();
		float p = 1.00f - r;
		
		//       p   0.00f
		if      (p < 0.250f) type = "scout";          // 25%
		else if (p < 0.500f) type = "longRangeScout"; // 25%
		else if (p < 0.700f) type = "battleGroup";    // 20%
		else if (p < 0.875f) type = "royalGuard";     // 17.5%
		else  /* p < 1.000f*/type = "jihadFleet";     // 12.5%
		
		CampaignFleetAPI fleet = getSector().createFleet("nomads", type);
		getLocation().spawnFleet(getAnchor(), 0, 0, fleet);
		fleet.addAssignment(FleetAssignment.RAID_SYSTEM, null, 10);
		
		return fleet;
	}

}
