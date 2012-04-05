package data.missions.test9;

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
		api.addToFleet(FleetSide.PLAYER, "medusa_PD", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hammerhead_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hound_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "lasher_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "broadsword_wing", FleetMemberType.FIGHTER_WING, true);
		api.addToFleet(FleetSide.PLAYER, "warthog_wing", FleetMemberType.FIGHTER_WING, true);
		api.addToFleet(FleetSide.PLAYER, "thunder_wing", FleetMemberType.FIGHTER_WING, true);
		api.addToFleet(FleetSide.PLAYER, "wasp_wing", FleetMemberType.FIGHTER_WING, true);
		api.addToFleet(FleetSide.PLAYER, "talon_wing", FleetMemberType.FIGHTER_WING, true);

		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "nom_iguana_wing", FleetMemberType.FIGHTER_WING, true);

		// Set up the map.
		float width = 6500f;
		float height = 4000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
	}

}






