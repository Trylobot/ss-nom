package data.scripts.world.systems;
import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import data.scripts.world.GenericWaypointArmadaController;
import java.awt.Color;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;

@SuppressWarnings( "unchecked" )
public class TheNomadsNur
{
	// this ratio is an observed ratio between the distances from Corvus to its third planet
	// in hyperspace vs. normal space
	private static final float hyperspace_compression = 0.0612f; // (459f / 7500f) // in pixels at default zoom
	
	private static final float star_jump_dist_factor_min = 0.8f;
	private static final float star_jump_dist_factor_max = 1.2f;
	
	
	public void generate(SectorAPI sector)
	{
		FactoryAPI factory = Global.getFactory();
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		// system
		StarSystemAPI system = sector.createStarSystem( "Nur" );
//		system.setBackgroundTextureFilename( "graphics/nom/backgrounds/background_nur.jpg" ); // doesn't look good
		system.setLightColor( new Color( 185, 185, 240 )); // light color in entire system, affects all entities
		system.getLocation().set( 0f, 0f ); // 18000f, -900f ); // DEBUG
		
		// stars, planets and moons
		SectorEntityToken system_center_of_mass = system.createToken( 0f, 0f );
		PlanetAPI star_A = system.addPlanet( system_center_of_mass, "Nur-A", "star_blue", 0f, 1000f, 1500f, 30f );
 		PlanetAPI star_B = system.addPlanet( system_center_of_mass, "Nur-B", "star_red", 180f, 300f, 600f, 30f );
		PlanetAPI planet_I = system.addPlanet( system_center_of_mass, "Naera", "desert", 45f, 300f, 8000f, 199f );
		PlanetAPI planet_I__moon_a = system.addPlanet( planet_I, "Ixaith", "rocky_unstable", 0f, 60f, 800f, 67f );
		PlanetAPI planet_I__moon_b = system.addPlanet( planet_I, "Ushaise", "rocky_ice", 45f, 45f, 1000f, 120f );
		PlanetAPI planet_I__moon_c = system.addPlanet( planet_I, "Riaze", "barren", 90f, 100f, 1200f, 130f );
		PlanetAPI planet_I__moon_d = system.addPlanet( planet_I, "Riaze-Tremn", "frozen", 135f, 35f, 1500f, 132f );
		PlanetAPI planet_I__moon_e = system.addPlanet( planet_I, "Eufariz", "frozen", 180f, 65f, 1750f, 200f );
		PlanetAPI planet_I__moon_f = system.addPlanet( planet_I, "Thumn", "rocky_ice", 225f, 100f, 2000f, 362f );
		
		// rings & bands
		system.addRingBand( planet_I, "misc", "rings1", 256f, 0, Color.white, 256f, 1300f, 30f );
		
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
//		system.autogenerateHyperspaceJumpPoints( true, true );
//		JumpPointAPI jump_A_from_system = factory.createJumpPoint( "Jump Point Alpha" );
//		OrbitAPI jump_orbit = factory.createCircularOrbit( planet_I, 275f, 525f, 90f );
//		jump_A_from_system.setOrbit( jump_orbit );
//		jump_A_from_system.setRelatedPlanet( planet_I );
//		jump_A_from_system.setStandardWormholeToHyperspaceVisual();
//		system.addEntity( jump_A_from_system );
//		
//		JumpPointAPI jump_A_from_hyper = factory.createJumpPoint( "Nur I" );
//		jump_A_from_hyper.getLocation().set( 0f, 0f );
//		hyper.addEntity( jump_A_from_hyper );
//		
//		system.setHyperspaceAnchor( jump_A_from_hyper );
		
		init_jump_anchor_near_planet( system, system_center_of_mass, planet_I, "Jump Point Alpha", 0f, 500f, 30f );
		
//		SectorEntityToken c2 = system.getEntityByName("Corvus II");
//		JumpPointAPI local_jump_point__from_planet_I = factory.createJumpPoint( "Jump Point Alpha" );
//		OrbitAPI orbit = factory.createCircularOrbit( planet_I, 0, 500, 30 );
//		local_jump_point__from_planet_I.setOrbit( orbit );
//		local_jump_point__from_planet_I.setRelatedPlanet( planet_I );
//		local_jump_point__from_planet_I.setStandardWormholeToHyperspaceVisual();
//		system.addEntity( local_jump_point__from_planet_I );
		
		
		//// can't use auto unless there's a main star set
		//system.autogenerateHyperspaceJumpPoints( true, true );
		
		init_star_gravitywell_jump_point( system, system_center_of_mass, star_A );
		init_star_gravitywell_jump_point( system, system_center_of_mass, star_B );
		// TODO: EveryFrameScript to update hyperspace anchors
		
		
//		JumpPointAPI hyperspace_jump_point__to_star_A = factory.createJumpPoint( "Nur-A Gravity Well" );
//		hyperspace_jump_point__to_star_A.setFaction( "nomads" ); // important? yes/no
//		JumpDestination jump_destination__star_A = new JumpDestination( star_A, "Order a jump to Nur-A" );
//		jump_destination__star_A.setMinDistFromToken( 0.8f * star_A.getRadius() );
//		jump_destination__star_A.setMaxDistFromToken( 1.2f * star_A.getRadius() );
//		hyperspace_jump_point__to_star_A.addDestination( jump_destination__star_A );
//		hyperspace_jump_point__to_star_A.setStandardWormholeToStarOrPlanetVisual( star_A );
//		hyperspace_jump_point__to_star_A.setRadius( hyperspace_to_space_ratio * star_A.getRadius() );
//		hyper.addEntity( hyperspace_jump_point__to_star_A );
//		Vector2f hyperspace_location__star_A = new Vector2f( star_A.getLocation() );
//		hyperspace_location__star_A.scale( hyperspace_to_space_ratio );
//		hyperspace_location__star_A.translate( system.getLocation().x, system.getLocation().y );
//		hyperspace_jump_point__to_star_A.getLocation().set( hyperspace_location__star_A ); // ???
//		
//		JumpPointAPI hyperspace_jump_point__to_star_B = factory.createJumpPoint( "Nur-B Gravity Well" );
//		hyperspace_jump_point__to_star_B.setFaction( "nomads" );
//		JumpDestination jump_destination__star_B = new JumpDestination( star_B, "Order a jump to Nur-B" );
//		jump_destination__star_B.setMinDistFromToken( 0.8f * star_B.getRadius() );
//		jump_destination__star_B.setMaxDistFromToken( 1.2f * star_B.getRadius() );
//		hyperspace_jump_point__to_star_B.addDestination( jump_destination__star_B );
//		hyperspace_jump_point__to_star_B.setStandardWormholeToStarOrPlanetVisual( star_B );
//		hyperspace_jump_point__to_star_B.setRadius( hyperspace_to_space_ratio * star_B.getRadius() );
//		hyper.addEntity( hyperspace_jump_point__to_star_B );
//		Vector2f hyperspace_location__star_B = new Vector2f( star_B.getLocation() );
//		hyperspace_location__star_B.scale( hyperspace_to_space_ratio );
//		hyperspace_location__star_B.translate( system.getLocation().x, system.getLocation().y );
//		hyperspace_jump_point__to_star_B.getLocation().set( hyperspace_location__star_B ); // ???


		
		
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
		GenericWaypointArmadaController nomad_armada_spawner_script =
			new GenericWaypointArmadaController(
				"nomads", // faction
				"colonyFleet", // VIP fleet
				sector, // global sector api
				system,  // system wherein armada should initially spawn
				8, // escort_fleet_count
				new String[]{ "scout", "longRangeScout", "battleGroup", "royalGuard", "jihadFleet" }, // escort_fleet_composition_pool
				new float[] {  0.250f,  0.250f,           0.200f,        0.175f,       0.125f      }, // escort_fleet_composition_weights
				GenericWaypointArmadaController.ESCORT_ORBIT, // escort formation type
				300.0f, // escort_formation_separation_distance (applies to all formations
				2, // waypoint_per_trip_minimum
				4, // waypoint_per_trip_maximum
				2, // waypoint_idle_time_days
				12 // out_of_sector_time_days
		);
		system.addScript( nomad_armada_spawner_script ); // automatic from here on out

		
		// relationships - none, hostile to all, including player
		FactionAPI nomads_faction = sector.getFaction( "nomads" );
		Object[] all_factions = sector.getAllFactions().toArray();
		for( int i = 0; i < all_factions.length; ++i )
		{
			FactionAPI cur_faction = (FactionAPI) all_factions[i];
			if( cur_faction != nomads_faction )
				nomads_faction.setRelationship( cur_faction.getId(), -1 );
		}
		
		nomads_faction.setRelationship( "player", 1 ); // DEBUG
    //		sector.getPlayerFleet().setLocation( 15000f, 15000f ); // DEBUG
		///

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
	
	private void init_star_gravitywell_jump_point( StarSystemAPI system, SectorEntityToken system_root, PlanetAPI star )
	{
		FactoryAPI factory = Global.getFactory();
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		JumpPointAPI jump_point = factory.createJumpPoint( star.getFullName()+" Gravity Well" );
		JumpDestination destination = new JumpDestination( star, star.getFullName() );
		destination.setMinDistFromToken( star_jump_dist_factor_min * star.getRadius() );
		destination.setMaxDistFromToken( star_jump_dist_factor_max * star.getRadius() );
		jump_point.addDestination( destination );
		jump_point.setStandardWormholeToStarOrPlanetVisual( star );
		hyper.addEntity( jump_point );
		
		update_hyperspace_jump_point_location( jump_point, system, system_root, star );
	}
	
	private void init_jump_anchor_near_planet( StarSystemAPI system, SectorEntityToken system_root, PlanetAPI planet, String name, float angle, float orbitRadius, float orbitDays )
	{
		FactoryAPI factory = Global.getFactory();
		LocationAPI hyper = Global.getSector().getHyperspace();

		JumpPointAPI local_jump_point = factory.createJumpPoint( name );
		OrbitAPI orbit = factory.createCircularOrbit( planet, angle, orbitRadius, orbitDays );
		local_jump_point.setOrbit( orbit );
		local_jump_point.setStandardWormholeToHyperspaceVisual();
		system.addEntity( local_jump_point );
		
		JumpPointAPI hyperspace_jump_point = factory.createJumpPoint( system.getName()+", "+name );
		JumpDestination destination = new JumpDestination( local_jump_point, system.getName()+", "+name );
		hyperspace_jump_point.addDestination( destination );
		hyperspace_jump_point.setStandardWormholeToStarOrPlanetVisual( planet );
		hyper.addEntity( hyperspace_jump_point );
				
		JumpDestination hyperspace_destination = new JumpDestination( hyperspace_jump_point, "Hyperspace" );
		local_jump_point.addDestination( hyperspace_destination );
		
		update_hyperspace_jump_point_location( hyperspace_jump_point, system, system_root, local_jump_point );
	}
	
	private void update_hyperspace_jump_point_location( JumpPointAPI jump_point, StarSystemAPI system, SectorEntityToken system_root, SectorEntityToken system_entity )
	{
//		jump_point.getLocation().set(
//			system.getLocation().x + (hyperspace_to_space_ratio * system_entity.getLocation().x),
//			system.getLocation().y + (hyperspace_to_space_ratio * system_entity.getLocation().y) ); // ???

		Vector2f location = new Vector2f( system.getLocation() );
		// loop through the orbital foci until the root is reached
		SectorEntityToken cursor = system_entity;
		while( cursor != system_root )
		{
			location.translate( 
				(hyperspace_compression * cursor.getLocation().x),
				(hyperspace_compression * cursor.getLocation().y) );
			cursor = cursor.getOrbit().getFocus();
		}
		jump_point.getLocation().set( location );
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
