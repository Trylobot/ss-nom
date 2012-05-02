package data.scripts.nom.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.fleet.*;

import data.scripts.world.*;

@SuppressWarnings( "unchecked" )
public class SectorGenWithNomads implements SectorGeneratorPlugin
{
	public void generate( SectorAPI sector )
	{
		StarSystemAPI system = sector.getStarSystem( "Corvus" );

		SectorEntityToken star = system.createToken( 0, 0 );
		SectorEntityToken planet = system.addPlanet( star, "Adum'Tulek", "desert", 90, 175, 3750, 150);

		NomadSpawnPoint spawner = new NomadSpawnPoint( sector, system, 1, 6, planet );
		system.addSpawnPoint( spawner );
		for( int i = 0; i < 6; i++ )
			spawner.spawnFleet();
		
		FactionAPI faction = sector.getFaction( "nomads" );
		faction.setRelationship( "player",                 -1 );
		faction.setRelationship( "hegemony",               -1 );
		faction.setRelationship( "tritachyon",             -1 );
		faction.setRelationship( "pirates",                -1 );
		faction.setRelationship( "independent",            -1 );
		faction.setRelationship( "junk_pirates",           -1 );
		faction.setRelationship( "interstellarFederation", -1 );
		faction.setRelationship( "lotus_pirates",          -1 );
	}
	
}
