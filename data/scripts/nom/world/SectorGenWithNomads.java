package data.scripts.nom.world;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CrewXPLevel;
import com.fs.starfarer.api.fleet.FleetMemberType;

import data.scripts.world.*;

@SuppressWarnings("unchecked")
public class SectorGenWithNomads extends SectorGen {

	public void generate(SectorAPI sector) {
		
    //super.generate( sector );
 		StarSystemAPI system = sector.getStarSystem("Corvus");
    
    NomadSpawnPoint nomadSpawn = new NomadSpawnPoint( sector, system, 10, 2, null );
    system.addSpawnPoint( nomadSpawn );
    for( int i = 0; i < 10; ++i )
      nomadSpawn.spawnFleet();
    
		FactionAPI hegemony = sector.getFaction("hegemony");
		FactionAPI tritachyon = sector.getFaction("tritachyon");
    FactionAPI nomads = sector.getFaction("nomads");
    nomads.setRelationship( hegemony.getId(), -1 );
    nomads.setRelationship( tritachyon.getId(), -1 );
    
	}
	
}
