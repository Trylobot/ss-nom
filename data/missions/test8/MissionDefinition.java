package data.missions.test8;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "HSS", FleetGoal.DEFEND, false);
		api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.DEFEND, true);

		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "nom_yellowjacket_sniper", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "nom_yellowjacket_sniper", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "nom_yellowjacket_sniper", FleetMemberType.SHIP, false);

		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);

		// Set up the map.
		float width = 6500f;
		float height = 4000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
	}

}






