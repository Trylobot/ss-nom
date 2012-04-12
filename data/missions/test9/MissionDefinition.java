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

		// Scarab testing
		//

		// Iguana testing
		api.addToFleet(FleetSide.PLAYER, "condor_FS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "wasp_wing", FleetMemberType.FIGHTER_WING, true);
		api.addToFleet(FleetSide.PLAYER, "talon_wing", FleetMemberType.FIGHTER_WING, true);
		
		api.addToFleet(FleetSide.ENEMY, "nom_iguana_wing", FleetMemberType.FIGHTER_WING, true);

		// Wurm testing
		//

		// // Komodo testing
		// api.addToFleet(FleetSide.PLAYER, "conquest_Elite", FleetMemberType.SHIP, false);
		// api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP, false);
		// api.addToFleet(FleetSide.PLAYER, "enforcer_Balanced", FleetMemberType.SHIP, false);
		// api.addToFleet(FleetSide.PLAYER, "medusa_Attack", FleetMemberType.SHIP, false);
		// api.addToFleet(FleetSide.PLAYER, "hammerhead_Balanced", FleetMemberType.SHIP, false);
		// api.addToFleet(FleetSide.PLAYER, "hammerhead_Elite", FleetMemberType.SHIP, false);
		// api.addToFleet(FleetSide.PLAYER, "gemini_Standard", FleetMemberType.SHIP, false);
		// api.addToFleet(FleetSide.PLAYER, "hound_Assault", FleetMemberType.SHIP, false);
		// api.addToFleet(FleetSide.PLAYER, "lasher_Assault", FleetMemberType.SHIP, false);
		// api.addToFleet(FleetSide.PLAYER, "thunder_wing", FleetMemberType.FIGHTER_WING, true);
		// api.addToFleet(FleetSide.PLAYER, "wasp_wing", FleetMemberType.FIGHTER_WING, true);
		// // 
		// api.addToFleet(FleetSide.ENEMY, "nom_komodo_assault", FleetMemberType.SHIP, true);


		// Set up the map.
		float width = 6000f;
		float height = 3500f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
	}

}






