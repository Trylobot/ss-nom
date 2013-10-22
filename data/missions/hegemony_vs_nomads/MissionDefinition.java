package data.missions.hegemony_vs_nomads;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin
{
	public void defineMission(MissionDefinitionAPI api)
	{
		//api.addPlugin( new TheNomadsCombatEnginePlugin() );
		
		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "TTS", FleetGoal.ATTACK, false);
		api.setFleetTagline(FleetSide.PLAYER, "Hegemony Forces");

		api.initFleet(FleetSide.ENEMY, "NA", FleetGoal.ATTACK, true);
		api.setFleetTagline(FleetSide.ENEMY, "The Nomad Armada");
		
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "onslaught_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "onslaught_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "onslaught_Outdated", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "conquest_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "conquest_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "eagle_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "falcon_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "falcon_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "venture_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "apogee_Balanced", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "dominator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dominator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dominator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dominator_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dominator_Outdated", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "condor_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "condor_FS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "eagle_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "eagle_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hammerhead_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hammerhead_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hammerhead_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hammerhead_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hound_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hound_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hound_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "lasher_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "lasher_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "lasher_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "lasher_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "brawler_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "brawler_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "brawler_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "brawler_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "brawler_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "thunder_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.PLAYER, "thunder_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.PLAYER, "broadsword_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.PLAYER, "broadsword_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.PLAYER, "gladius_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.PLAYER, "gladius_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.PLAYER, "talon_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.PLAYER, "talon_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.PLAYER, "talon_wing", FleetMemberType.FIGHTER_WING, false);

		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "nom_oasis_standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_gila_monster_antibattleship", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_gila_monster_antibattleship", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_sandstorm_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_sandstorm_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_rattlesnake_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_rattlesnake_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_rattlesnake_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_scorpion_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_scorpion_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_scorpion_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_komodo_mk2_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_komodo_mk2_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_komodo_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_komodo_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_komodo_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_flycatcher_carrier", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_flycatcher_carrier", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_death_bloom_strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_death_bloom_strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_death_bloom_strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_yellowjacket_sniper", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_yellowjacket_sniper", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_yellowjacket_sniper", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_wurm_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_wurm_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_wurm_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_wurm_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_wurm_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_wurm_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nom_fang_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_fang_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_fang_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_toad_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_toad_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_toad_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_toad_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_toad_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_toad_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_iguana_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_iguana_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_iguana_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_iguana_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_iguana_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_iguana_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_iguana_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_iguana_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_scarab_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_scarab_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_scarab_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_scarab_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_scarab_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.ENEMY, "nom_scarab_wing", FleetMemberType.FIGHTER_WING, false);
		
		// Set up the map.
		float width = 20000f;
		float height = 26000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		
		for (int i = 0; i < 50; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 400f; 
			api.addNebula(x, y, radius);
		}
		
		// Add objectives
		api.addObjective(minX + width * 0.25f, minY + height * 0.25f, "nav_buoy");
		api.addObjective(minX + width * 0.75f, minY + height * 0.25f, "comm_relay");
		api.addObjective(minX + width * 0.75f, minY + height * 0.75f, "nav_buoy");
		api.addObjective(minX + width * 0.25f, minY + height * 0.75f, "comm_relay");
		api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");
		
		// // Add a randomized asteroid field, maybe
		// if ((float) Math.random() > 0.5f) {
		// 	api.addAsteroidField(10f + (float) Math.random() * (width - 20f), 10f + (float) Math.random() * (height - 20f), (float) Math.random() * 360f, (float) Math.random() * 1000 + 1000,
		// 						 20f, 70f, (int) ((float) Math.random() * 100 + 50));
		// }
	}

}






