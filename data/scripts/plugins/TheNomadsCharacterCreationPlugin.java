package data.scripts.plugins;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI.CrewXPLevel;
import com.fs.starfarer.api.characters.CharacterCreationPlugin;
import com.fs.starfarer.api.characters.CharacterCreationPlugin.Response;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import java.util.ArrayList;
import java.util.List;

public class TheNomadsCharacterCreationPlugin implements CharacterCreationPlugin
{
	public static class ResponseImpl implements Response
	{
		private String text;
		public ResponseImpl(String text) {
			this.text = text;
		}
		public String getText() {
			return text;
		}
	}
	
	private int stage = 0;
	// 0
	private ResponseImpl SUPPLY_OFFICER = new ResponseImpl("Served as a junior supply officer in an independent system's navy");
	private ResponseImpl GUNNER = new ResponseImpl("Hired out as a gunner on a mercenary ship");
	private ResponseImpl ENGINEER = new ResponseImpl("Found employment as an assistant engineer on an exploration vessel");
	private ResponseImpl COPILOT = new ResponseImpl("Spent time as a co-pilot on a patrol ship in an independent system");
	private ResponseImpl SOMETHING_ELSE_1 = new ResponseImpl("Did something else");
	// 1
	private ResponseImpl ADJUTANT = new ResponseImpl("Served as an adjutant in the Hegemony Navy");
	private ResponseImpl QUARTERMASTER = new ResponseImpl("Performed the duties of a quartermaster on an independent warship");
	private ResponseImpl HELM = new ResponseImpl("Helmed a patrol ship operating in a backwater system");
	private ResponseImpl COMBAT_ENGINEER = new ResponseImpl("Took over the duties of chief combat engineer during a lengthy campaign");
	private ResponseImpl SOMETHING_ELSE_2 = new ResponseImpl("Did something else");
	
	private String [] prompts = new String [] {
		"Early in your career, you...",
		"More recently, you...",
	};
	
	public String getPrompt() {
		if (stage < prompts.length) return prompts[stage];
		return null;
	}

	@SuppressWarnings("unchecked")
	public List getResponses() {
		List result = new ArrayList();
		if (stage == 0) {
			result.add(SUPPLY_OFFICER);
			result.add(GUNNER);
			result.add(ENGINEER);
			result.add(COPILOT);
			result.add(SOMETHING_ELSE_1);
		} else if (stage == 1) {
			result.add(ADJUTANT);
			result.add(QUARTERMASTER);
			result.add(HELM);
			result.add(COMBAT_ENGINEER);
			result.add(SOMETHING_ELSE_2);
		}
		return result;
	}

	
	public void submit(Response response, CharacterCreationData data) {
		if (stage == 0) { // just in case
			data.addStartingShipChoice("shuttle_Attack");
			data.setStartingLocationName("Corvus");
			data.getStartingCoordinates().set(-2500, -3500);
			
			if (Global.getSettings().isDevMode() && false) {
				data.addStartingShipChoice("tempest_Attack");
				data.addStartingFleetMember("odyssey_Balanced", FleetMemberType.SHIP);
				data.addStartingFleetMember("enforcer_Elite", FleetMemberType.SHIP);
				data.addStartingFleetMember("venture_Balanced", FleetMemberType.SHIP);
				data.addStartingFleetMember("crig_Standard", FleetMemberType.SHIP);
				data.addStartingFleetMember("ox_Standard", FleetMemberType.SHIP);
				data.addStartingFleetMember("wasp_wing", FleetMemberType.FIGHTER_WING);
				data.addStartingFleetMember("wasp_wing", FleetMemberType.FIGHTER_WING);
				data.getStartingCargo().getCredits().add(50000f);
				data.getStartingCargo().addFuel(1000);
				data.getStartingCargo().addSupplies(800);
				data.getStartingCargo().addMarines(100);
				data.getStartingCargo().addCrew(CrewXPLevel.REGULAR, 800);
			}
		}
		
		stage++;
		
		MutableCharacterStatsAPI stats = data.getPerson().getStats();
		if (response == SUPPLY_OFFICER) {
			stats.increaseAptitude("leadership");
			stats.increaseSkill("fleet_logistics");
			stats.increaseSkill("command_experience");
			data.getStartingCargo().getCredits().add(3000f);
		} else if (response == GUNNER) {
			stats.increaseAptitude("combat");
			stats.increaseSkill("ordnance_expert");
			stats.increaseSkill("target_analysis");
			data.getStartingCargo().getCredits().add(3000f);
		} else if (response == ENGINEER) {
			stats.increaseAptitude("technology");
			stats.increaseSkill("field_repairs");
			stats.increaseSkill("mechanical_engineering");
			data.getStartingCargo().getCredits().add(3000f);
		} else if (response == COPILOT) {
			stats.increaseAptitude("combat");
			stats.increaseSkill("helmsmanship");
			stats.increaseSkill("evasive_action");
			data.getStartingCargo().getCredits().add(3000f);
		} else if (response == SOMETHING_ELSE_1) {
			stats.addAptitudePoints(1);
			stats.addSkillPoints(2);
			data.getStartingCargo().getCredits().add(1000f);
		} 
		
		else if (response == ADJUTANT) {
			stats.increaseAptitude("leadership");
			stats.increaseSkill("fleet_logistics");
			stats.increaseSkill("advanced_tactics");
			data.addStartingShipChoice("lasher_Standard");
			data.getStartingCargo().getCredits().add(3000f);
		} else if (response == QUARTERMASTER) {
			stats.increaseAptitude("technology");
			stats.increaseSkill("navigation");
			stats.addSkillPoints(1);
			data.addStartingShipChoice("wolf_CS");
			data.getStartingCargo().getCredits().add(3000f);
		} else if (response == HELM) {
			stats.increaseAptitude("combat");
			stats.increaseSkill("helmsmanship");
			stats.increaseSkill("evasive_action");
			data.addStartingShipChoice("vigilance_Standard");
			data.getStartingCargo().getCredits().add(3000f);
		} else if (response == COMBAT_ENGINEER) {
			stats.increaseAptitude("combat");
			stats.increaseSkill("damage_control");
			stats.increaseSkill("flux_modulation");
			data.addStartingShipChoice("lasher_Standard");
			data.getStartingCargo().getCredits().add(3000f);
		} else if (response == SOMETHING_ELSE_2) {
			stats.addAptitudePoints(1);
			stats.addSkillPoints(2);
			data.addStartingShipChoice("hound_Assault");
			data.getStartingCargo().getCredits().add(1000f);
		}
	}

	public void startingShipPicked(String variantId, CharacterCreationData data) {
		MutableCharacterStatsAPI stats = data.getPerson().getStats();
		stats.addAptitudePoints(1);
		stats.addSkillPoints(2);
		
		if (variantId.equals("vigilance_Standard")) {
			data.getStartingCargo().addFuel(20);
			data.getStartingCargo().addSupplies(30);
			data.getStartingCargo().addCrew(CrewXPLevel.REGULAR, 20);
			data.getStartingCargo().addMarines(5);
		} else
		if (variantId.equals("lasher_Standard")) {
			data.getStartingCargo().addFuel(20);
			data.getStartingCargo().addSupplies(20);
			data.getStartingCargo().addCrew(CrewXPLevel.REGULAR, 40);
			data.getStartingCargo().addMarines(10);
		} else
		if (variantId.equals("wolf_CS")) {
			data.getStartingCargo().addFuel(20);
			data.getStartingCargo().addSupplies(20);
			data.getStartingCargo().addCrew(CrewXPLevel.REGULAR, 22);
			data.getStartingCargo().addMarines(7);
		} else
		if (variantId.equals("shuttle_Attack")) {
			data.getStartingCargo().addFuel(10);
			data.getStartingCargo().addSupplies(10);
			data.getStartingCargo().addCrew(CrewXPLevel.REGULAR, 10);
			data.getStartingCargo().addMarines(3);
		} else				
		if (variantId.equals("hound_Assault")) {
			data.getStartingCargo().addFuel(30);
			data.getStartingCargo().addSupplies(40);
			data.getStartingCargo().addCrew(CrewXPLevel.REGULAR, 25);
			data.getStartingCargo().addMarines(15);
		} else
		if (variantId.equals("tempest_Attack")) {
			data.getStartingCargo().addFuel(20);
			data.getStartingCargo().addSupplies(30);
			data.getStartingCargo().addCrew(CrewXPLevel.ELITE, 20);
			data.getStartingCargo().addMarines(0);
		} else {
			data.getStartingCargo().addFuel(20);
			data.getStartingCargo().addSupplies(20);
			data.getStartingCargo().addCrew(CrewXPLevel.REGULAR, 20);
			data.getStartingCargo().addMarines(10);
		}
	}

}







