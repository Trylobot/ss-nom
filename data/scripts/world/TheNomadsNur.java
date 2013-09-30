package data.scripts.world;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import java.awt.Color;

@SuppressWarnings( "unchecked" )
public class TheNomadsNur
{
	public void generate(SectorAPI sector)
	{
		StarSystemAPI system = sector.createStarSystem("Nur");
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/nom/backgrounds/background_nur.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI star = system.initStar("star_blue", // id in planets.json
										 1000f, 		// radius (in pixels at default zoom)
										 18000, -900);   // location in hyperspace
		
		system.setLightColor(new Color(180, 180, 250)); // light color in entire system, affects all entities
		

		/*
		 * addPlanet() parameters:
		 * 1. What the planet orbits (orbit is always circular)
		 * 2. Name
		 * 3. Planet type id in planets.json
		 * 4. Starting angle in orbit, i.e. 0 = to the right of the star
		 * 5. Planet radius, pixels at default zoom
		 * 6. Orbit radius, pixels at default zoom
		 * 7. Days it takes to complete an orbit. 1 day = 10 seconds.
		 */
//		PlanetAPI a1 = system.addPlanet(star, "Sindria", "rocky_metallic", 0, 150, 2500, 100);
//		PlanetAPI a2 = system.addPlanet(star, "Salus", "gas_giant", 230, 350, 7000, 250);
//		
//		PlanetAPI a21 = system.addPlanet(a2, "Cruor", "rocky_unstable", 45, 80, 800, 25);
//		PlanetAPI a22 = system.addPlanet(a2, "Volturn", "water", 110, 120, 1400, 45);
//		
//		PlanetAPI a3 = system.addPlanet(star, "Umbra", "rocky_ice", 280, 150, 12000, 650);
//		
//		a1.setCustomDescriptionId("planet_sindria");
//		a2.setCustomDescriptionId("planet_salus");
//		a21.setCustomDescriptionId("planet_cruor");
//		a22.setCustomDescriptionId("planet_volturn");
//		a3.setCustomDescriptionId("planet_umbra");
//		
//		a2.getSpec().setPlanetColor(new Color(255,215,190,255));
//		a2.getSpec().setAtmosphereColor(new Color(160,110,45,140));
//		a2.getSpec().setCloudColor(new Color(255,164,96,200));
//		a2.getSpec().setTilt(15);
//		a2.applySpecChanges();
//		
//		/*
//		 * addAsteroidBelt() parameters:
//		 * 1. What the belt orbits
//		 * 2. Number of asteroids
//		 * 3. Orbit radius
//		 * 4. Belt width
//		 * 6/7. Range of days to complete one orbit. Value picked randomly for each asteroid. 
//		 */
//		system.addAsteroidBelt(a2, 50, 1100, 128, 40, 80);
//		
//		
//		/*
//		 * addRingBand() parameters:
//		 * 1. What it orbits
//		 * 2. Category under "graphics" in settings.json
//		 * 3. Key in category
//		 * 4. Width of band within the texture
//		 * 5. Index of band
//		 * 6. Color to apply to band
//		 * 7. Width of band (in the game)
//		 * 8. Orbit radius (of the middle of the band)
//		 * 9. Orbital period, in days
//		 */
//		system.addRingBand(a2, "misc", "rings1", 256f, 2, Color.white, 256f, 1100, 40f);
//		system.addRingBand(a2, "misc", "rings1", 256f, 2, Color.white, 256f, 1100, 60f);
//		system.addRingBand(a2, "misc", "rings1", 256f, 2, Color.white, 256f, 1100, 80f);
//		
////		system.addRingBand(a2, "misc", "rings1", 256f, 0, Color.white, 256f, 1700, 50f);
////		system.addRingBand(a2, "misc", "rings1", 256f, 0, Color.white, 256f, 1700, 70f);
////		system.addRingBand(a2, "misc", "rings1", 256f, 1, Color.white, 256f, 1700, 90f);
////		system.addRingBand(a2, "misc", "rings1", 256f, 1, Color.white, 256f, 1700, 110f);
//		
//		system.addRingBand(a2, "misc", "rings1", 256f, 3, Color.white, 256f, 1800, 70f);
//		system.addRingBand(a2, "misc", "rings1", 256f, 3, Color.white, 256f, 1800, 90f);
//		system.addRingBand(a2, "misc", "rings1", 256f, 3, Color.white, 256f, 1800, 110f);
//		
//		system.addRingBand(a2, "misc", "rings1", 256f, 0, Color.white, 256f, 2150, 50f);
//		system.addRingBand(a2, "misc", "rings1", 256f, 0, Color.white, 256f, 2150, 70f);
//		system.addRingBand(a2, "misc", "rings1", 256f, 0, Color.white, 256f, 2150, 80f);
//		system.addRingBand(a2, "misc", "rings1", 256f, 1, Color.white, 256f, 2100, 90f);
//		
//		
//		
//		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("Jump Point Alpha");
//		OrbitAPI orbit = Global.getFactory().createCircularOrbit(a1, 0, 500, 30);
//		jumpPoint.setOrbit(orbit);
//		jumpPoint.setRelatedPlanet(a1);
//		jumpPoint.setStandardWormholeToHyperspaceVisual();
//		system.addEntity(jumpPoint);
//		
//		
//		
//		SectorEntityToken station = system.addOrbitalStation(a1, 45, 300, 50, "Command & Control", "sindrian_diktat");
//		initStationCargo(station);
//		
//		// example of using custom visuals below
////		a1.setCustomInteractionDialogImageVisual(new InteractionDialogImageVisual("illustrations", "hull_breach", 800, 800));
////		jumpPoint.setCustomInteractionDialogImageVisual(new InteractionDialogImageVisual("illustrations", "space_wreckage", 1200, 1200));
////		station.setCustomInteractionDialogImageVisual(new InteractionDialogImageVisual("illustrations", "cargo_loading", 1200, 1200));
//		
//		// generates hyperspace destinations for in-system jump points
//		system.autogenerateHyperspaceJumpPoints(true, true);
		
		///////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////
		
//		StarSystemAPI system = sector.getStarSystem( "Corvus" );
//		
//		GenericWaypointArmadaController spawner = new GenericWaypointArmadaController(
//			"nomads",
//			"colonyFleet",
//			sector, 
//			system, 
//		  8, /* escort_fleet_count */
//			new String[]{ "scout", "longRangeScout", "battleGroup", "royalGuard", "jihadFleet" }, /* escort_fleet_composition_pool */
//			new float[] {  0.250f,  0.250f,           0.200f,        0.175f,       0.125f      }, /* escort_fleet_composition_weights */
//			GenericWaypointArmadaController.ESCORT_ORBIT, /* escort_formation */
//			300.0f, /* escort_formation_separation_distance */
//			2, /* waypoint_per_trip_minimum */
//			4, /* waypoint_per_trip_maximum */
//			2, /* waypoint_idle_time_days */
//			12 /* out_of_sector_time_days */ );
//		system.addSpawnPoint( spawner ); // automatic from here on out
//
//		// relationships
//		FactionAPI faction = sector.getFaction( "nomads" );
//		faction.setRelationship( "player", -1 );
//		faction.setRelationship( "hegemony", -1 );
//		faction.setRelationship( "tritachyon", -1 );
//		faction.setRelationship( "pirates",  -1 );
//		faction.setRelationship( "independent", -1 );
//			
//		
//		/// DEBUGGING ONLY
//		faction.setRelationship( "player", 1 );
//    //		sector.getPlayerFleet().setLocation( 15000f, 15000f );
//		///

		///////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////


//		DiktatPatrolSpawnPoint patrolSpawn = new DiktatPatrolSpawnPoint(sector, system, 5, 3, a1);
//		system.addScript(patrolSpawn);
//		for (int i = 0; i < 5; i++)
//			patrolSpawn.spawnFleet();
//
//		DiktatGarrisonSpawnPoint garrisonSpawn = new DiktatGarrisonSpawnPoint(sector, system, 30, 1, a1, a1);
//		system.addScript(garrisonSpawn);
//		garrisonSpawn.spawnFleet();
//		
//		
//		system.addScript(new IndependentTraderSpawnPoint(sector, hyper, 1, 10, hyper.createToken(-6000, 2000), station));
	}
	
	
//	private void initStationCargo(SectorEntityToken station)
//	{
//		CargoAPI cargo = station.getCargo();
//		addRandomWeapons(cargo, 5);
//		
//		cargo.addCrew(CrewXPLevel.VETERAN, 20);
//		cargo.addCrew(CrewXPLevel.REGULAR, 500);
//		cargo.addMarines(200);
//		cargo.addSupplies(1000);
//		cargo.addFuel(500);
//		
//		cargo.getMothballedShips().addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "conquest_Hull"));
//		cargo.getMothballedShips().addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "crig_Hull"));
//		cargo.getMothballedShips().addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "crig_Hull"));
//		cargo.getMothballedShips().addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "crig_Hull"));
//		cargo.getMothballedShips().addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "ox_Hull"));
//		cargo.getMothballedShips().addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "ox_Hull"));
//		cargo.getMothballedShips().addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "ox_Hull"));
//		cargo.getMothballedShips().addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "ox_Hull"));
//		cargo.getMothballedShips().addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "ox_Hull"));
//		cargo.getMothballedShips().addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.FIGHTER_WING, "gladius_wing"));
//		cargo.getMothballedShips().addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.FIGHTER_WING, "gladius_wing"));
//	}
	
//	private void addRandomWeapons(CargoAPI cargo, int count)
//	{
//		List weaponIds = Global.getSector().getAllWeaponIds();
//		for (int i = 0; i < count; i++)
//		{
//			String weaponId = (String) weaponIds.get((int) (weaponIds.size() * Math.random()));
//			int quantity = (int)(Math.random() * 4f + 2f);
//			cargo.addWeapons(weaponId, quantity);
//		}
//	}
	
}
