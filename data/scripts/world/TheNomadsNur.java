package data.scripts.world;
import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import java.awt.Color;
import java.util.List;

@SuppressWarnings( "unchecked" )
public class TheNomadsNur
{
	public void generate(SectorAPI sector)
	{
		FactoryAPI factory = Global.getFactory();
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		// system
		StarSystemAPI system = sector.createStarSystem( "Nur" );
//		system.getLocation().set( 18000f, -900f ); // location in hyperspace
		system.initStar( "star_blue", 1000f, 18000f, -900f );
		system.setBackgroundTextureFilename( "graphics/nom/backgrounds/background_nur.jpg" );
		system.setLightColor( new Color( 180, 180, 250 )); // light color in entire system, affects all entities
		
		// stars, planets and moons
		SectorEntityToken system_center_of_mass = system.createToken( 0f, 0f );
		PlanetAPI star_A = system.addPlanet( system_center_of_mass, "Nur-A", "star_blue", 0f, 1000f, 1500f, 224f );
		PlanetAPI star_B = system.addPlanet( system_center_of_mass, "Nur-B", "star_red", 180f, 300f, 450f, 224f );
		PlanetAPI planet_I = system.addPlanet( system_center_of_mass, "Nur I", "desert", 45f, 400f, 4500f, 772f );
		PlanetAPI planet_I__moon_a = system.addPlanet( planet_I, "Nur I-a", "rocky_unstable", 0f, 80f, 800f, 67f );
		PlanetAPI planet_I__moon_b = system.addPlanet( planet_I, "Nur I-b", "rocky_ice", 45f, 60f, 1000f, 120f );
		PlanetAPI planet_I__moon_c = system.addPlanet( planet_I, "Nur I-c", "barren", 90f, 130f, 1200f, 130f );
		PlanetAPI planet_I__moon_d = system.addPlanet( planet_I, "Nur I-d", "rocky_ice", 135f, 40f, 1500f, 132f );
		PlanetAPI planet_I__moon_e = system.addPlanet( planet_I, "Nur I-e", "frozen", 180f, 80f, 1750f, 200f );
		PlanetAPI planet_I__moon_f = system.addPlanet( planet_I, "Nur I-f", "frozen", 225f, 100f, 2000f, 362f );
		
		
		
		// rings & bands
		system.addRingBand( planet_I, "misc", "rings1", 256f, 0, Color.white, 128f, 1300f, 90f );
		
		// descriptions
//		star_A.setCustomDescriptionId("nom_star_nur_alpha");
//		star_B.setCustomDescriptionId("nom_star_nur_beta");
//		planet_I.setCustomDescriptionId("nom_planet_I");
//		planet_I__moon_a.setCustomDescriptionId("nom_planet_I__moon_a");
//		planet_I__moon_b.setCustomDescriptionId("nom_planet_I__moon_b");
//		planet_I__moon_c.setCustomDescriptionId("nom_planet_I__moon_c");
//		planet_I__moon_d.setCustomDescriptionId("nom_planet_I__moon_d");
//		planet_I__moon_e.setCustomDescriptionId("nom_planet_I__moon_e");
//		planet_I__moon_f.setCustomDescriptionId("nom_planet_I__moon_f");
		
		// inline spec changes
//    planet_I.getSpec().setPlanetColor( new Color( 255,215,255, 255 ));
		planet_I.getSpec().setAtmosphereColor( new Color( 160,110,45, 140 ));
		planet_I.getSpec().setCloudColor( new Color( 255,255,255, 23 ));
		planet_I.getSpec().setTilt( 15 );
		planet_I.applySpecChanges();
		
		// jump points
		system.autogenerateHyperspaceJumpPoints( true, true );
//		JumpPointAPI jump_A_from_system = factory.createJumpPoint( "Jump Point Alpha" );
//		OrbitAPI jump_orbit = factory.createCircularOrbit( planet_I, 275f, 525f, 90f );
//		jump_A_from_system.setOrbit( jump_orbit );
//		jump_A_from_system.setRelatedPlanet( planet_I );
//		jump_A_from_system.setStandardWormholeToHyperspaceVisual();
//		system.addEntity( jump_A_from_system );
//		
//		JumpPointAPI jump_A_from_hyper = factory.createJumpPoint( "Nur I" );
//		jump_A_from_hyper.getLocation().set( 0, 0 );
//		hyper.addEntity( jump_A_from_hyper );
//		
//		system.setHyperspaceAnchor( jump_A_from_hyper );
		
		
		// asteroids
//		system.addAsteroidBelt(a2, 50, 1100, 128, 40, 80);

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
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////
		
//		StarSystemAPI system = sector.getStarSystem( "Corvus" );
//		
		GenericWaypointArmadaController nomad_armada_spawner = new GenericWaypointArmadaController(
			"nomads",
			"colonyFleet",
			sector, 
			system, 
		  8, /* escort_fleet_count */
			new String[]{ "scout", "longRangeScout", "battleGroup", "royalGuard", "jihadFleet" }, /* escort_fleet_composition_pool */
			new float[] {  0.250f,  0.250f,           0.200f,        0.175f,       0.125f      }, /* escort_fleet_composition_weights */
			GenericWaypointArmadaController.ESCORT_ORBIT, /* escort_formation */
			300.0f, /* escort_formation_separation_distance */
			2, /* waypoint_per_trip_minimum */
			4, /* waypoint_per_trip_maximum */
			2, /* waypoint_idle_time_days */
			12 /* out_of_sector_time_days */ );
		system.addSpawnPoint( nomad_armada_spawner ); // automatic from here on out

		// relationships - none, hostile to all, including player
		FactionAPI nomads_faction = sector.getFaction( "nomads" );
		List<FactionAPI> all_factions = sector.getAllFactions();
		for( FactionAPI cur_faction : all_factions )
		{
			if( cur_faction != nomads_faction )
				nomads_faction.setRelationship( cur_faction.getId(), -1 );
		}
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
