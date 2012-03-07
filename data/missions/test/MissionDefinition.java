package data.missions.test;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "HSS", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true);

		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "nom_gila_monster_antibattleship", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "nom_komodo_assault", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "nom_wurm_assault", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "nom_iguana_assault", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "nom_scarab_assault", FleetMemberType.FIGHTER_WING, false);

		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hound_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		
		// Set up the map.
		float width = 10000f;
		float height = 5000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
	}

}






