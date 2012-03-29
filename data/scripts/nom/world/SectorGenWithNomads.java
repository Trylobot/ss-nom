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

@SuppressWarnings( "unchecked" )
public class SectorGenWithNomads extends SectorGen
{
	public void generate( SectorAPI sector )
	{
		StarSystemAPI system = sector.getStarSystem( "Corvus" );

		// changed name of planet and moved away from star
		SectorEntityToken star = system.createToken( 0, 0 );
		SectorEntityToken Adum_Tulek = system.addPlanet( star, "Adum'Tulek", "desert", 90, 450, 4000, 400 );
		NomadSpawnPoint nomadSpawn = new NomadSpawnPoint( sector, system, 10, 2, Adum_Tulek );
		system.addSpawnPoint( nomadSpawn );
		
		// spawn points changed from 10 --> 8
		system.addSpawnPoint( nomadSpawn );
		for( int i = 0; i < 8; ++i )
		{
		  nomadSpawn.spawnFleet();
		}
		
		// faction api's referenced by string to increase standalone compatibility
		FactionAPI nomads = sector.getFaction( "nomads" );
		nomads.setRelationship( "hegemony",     -1 );
		nomads.setRelationship( "tritachyon",   -1 );
		nomads.setRelationship( "pirates",      -1 );
		nomads.setRelationship( "independent",  -1 );
		nomads.setRelationship( "nomads",       -1 );
		nomads.setRelationship( "junk_pirates", -1 );
		nomads.setRelationship( "player",       -1 );
	}
	
}
