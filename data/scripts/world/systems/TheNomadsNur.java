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
import data.scripts.world.armada.CampaignArmadaController;
import data.scripts.world.armada.CampaignArmadaFormationOrbit;
import data.scripts.world.armada.CampaignArmadaResourceSharingController;
import data.scripts.world.armada.api.CampaignArmadaEscortFleetPositionerAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

@SuppressWarnings( "unchecked" )
public class TheNomadsNur
{
	private FactoryAPI factory;
	
	private SectorAPI sector;
	private LocationAPI hyper;
	
	private StarSystemAPI system;
	private SectorEntityToken system_center_of_mass;
	private PlanetAPI star_A;
	private PlanetAPI star_B;
	private PlanetAPI planet_I;
	private PlanetAPI planet_I__moon_a;
	private PlanetAPI planet_I__moon_b;
	private PlanetAPI planet_I__moon_c;
	private PlanetAPI planet_I__moon_d;
	private PlanetAPI planet_I__moon_e;
	private PlanetAPI planet_I__moon_f;
	

	public void generate( SectorAPI sector )
	{
		// globals
		this.sector = sector;
		factory = Global.getFactory();
		hyper = sector.getHyperspace();
		
		// system
		system = sector.createStarSystem( "Nur" );
        //system.setBackgroundTextureFilename( "graphics/nom/backgrounds/background_nur.jpg" ); // doesn't look good
		system.setLightColor( new Color( 185, 185, 240 )); // light color in entire system, affects all entities
		system.getLocation().set( 18000f, -900f );
		
		// stars, planets, moons, jump points
		init_celestial_bodies( system );
		
		// spawners and other fleet related scripts
		init_fleet_scripts( sector, system );
		
		// faction relationships
		init_faction_relationships( sector );
	}
	
	private static final float star_jump_dist_factor_min = 0.8f;
	private static final float star_jump_dist_factor_max = 1.2f;
	
	private void init_celestial_bodies( StarSystemAPI system )
	{
		// stars, planets and moons
		system_center_of_mass = system.createToken( 0f, 0f );
		star_A = system.addPlanet( system_center_of_mass, "Nur-A", "star_blue", 90f, 1000f, 1500f, 30f );
 		star_B = system.addPlanet( system_center_of_mass, "Nur-B", "star_red", 270f, 300f, 600f, 30f );
		planet_I = system.addPlanet( system_center_of_mass, "Naera", "desert", 45f, 300f, 8000f, 199f );
		planet_I__moon_a = system.addPlanet( planet_I, "Ixaith", "rocky_unstable", 0f, 60f, 800f, 67f );
		planet_I__moon_b = system.addPlanet( planet_I, "Ushaise", "rocky_ice", 45f, 45f, 1000f, 120f );
		planet_I__moon_c = system.addPlanet( planet_I, "Riaze", "barren", 90f, 100f, 1200f, 130f );
		planet_I__moon_d = system.addPlanet( planet_I, "Riaze-Tremn", "frozen", 135f, 35f, 1500f, 132f );
		planet_I__moon_e = system.addPlanet( planet_I, "Eufariz", "frozen", 180f, 65f, 1750f, 200f );
		planet_I__moon_f = system.addPlanet( planet_I, "Thumn", "rocky_ice", 225f, 100f, 2000f, 362f );
		
		// specs
		planet_I.getSpec().setAtmosphereColor( new Color( 160,110,45, 140 ));
		planet_I.getSpec().setCloudColor( new Color( 255,255,255, 23 ));
		planet_I.getSpec().setTilt( 15 );
		planet_I.applySpecChanges();
		
		// rings & bands
		system.addRingBand( planet_I, "misc", "rings1", 256f, 0, Color.white, 256f, 630f, 30f );
		
		// jump points
		init_star_gravitywell_jump_point( system, system_center_of_mass, star_A, 
		  star_jump_dist_factor_min, star_jump_dist_factor_max );
		
		init_star_gravitywell_jump_point( system, system_center_of_mass, star_B,
		  star_jump_dist_factor_min, star_jump_dist_factor_max);
		
		init_jump_anchor_near_planet( system, system_center_of_mass, planet_I, "Jump Point Alpha", 0f, 500f, 30f );
		// TODO: EveryFrameScript to update hyperspace anchors (not sure if base game is doing this yet)

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
	}
	
	private void init_fleet_scripts( SectorAPI sector, StarSystemAPI system )
	{
		// armada formation type
		CampaignArmadaEscortFleetPositionerAPI armada_formation =
			new CampaignArmadaFormationOrbit(
				sector,
				300.0f, // orbitRadius
				1.0f, // orbitDirection
				0.25f // orbitPeriodDays
			);
		
		// armada composition data (references faction definition data)
		String[] escort_pool = { "scout", "longRangeScout", "battleGroup", "royalGuard", "jihadFleet" };
		float[] escort_weights={  0.250f,  0.250f,           0.200f,        0.175f,       0.125f      };
		
		// armada waypoint controller script
		CampaignArmadaController nomad_armada =
			new CampaignArmadaController(
				"nomads", // faction
				"colonyFleet", // VIP fleet
				sector, // global sector api
				planet_I__moon_f,
				8, // escort_fleet_count
				escort_pool,
				escort_weights,
				armada_formation,
				2, // waypoint_per_trip_minimum
				5, // waypoint_per_trip_maximum
				2, // waypoint_idle_time_days
				10.0f, // distance to waypoint in order to start idling
				12 // dead_time_days
			);
		system.addScript( nomad_armada );
		
		// armada resource pooling script
		CampaignArmadaResourceSharingController armada_resource_pool = 
			new CampaignArmadaResourceSharingController( 
				sector, 
				nomad_armada,
				3.0f, // 3 days at fleet's current usage (whatever it happens to be)
				0.10f, // skeleton crew requirement, plus 10%
				3.0f, // 5 light-years worth of fuel at fleet's current fuel consumption rate
				12.0f, // 12 days at fleet's current usage (whatever it happens to be)
				0.50f, // skeleton crew requirement, plus 25%
				20.0f // 15 light-years worth of fuel at fleet's current fuel consumption rate
			);
		system.addScript( armada_resource_pool );
		
	}
	
	private void init_faction_relationships( SectorAPI sector )
	{
		// relationships - none, hostile to all, including player
		FactionAPI nomads_faction = sector.getFaction( "nomads" );
		Object[] all_factions = sector.getAllFactions().toArray();
		for( int i = 0; i < all_factions.length; ++i )
		{
			FactionAPI cur_faction = (FactionAPI) all_factions[i];
			if( cur_faction != nomads_faction )
				nomads_faction.setRelationship( cur_faction.getId(), -1 );
		}
		
		/////////////////////////////////////////////////////////////
		if( Global.getSettings().isDevMode() )
			nomads_faction.setRelationship( "player", 1 ); // DEBUG: FRIENDLY
	}
	
	
	private void init_star_gravitywell_jump_point( StarSystemAPI system, SectorEntityToken system_root, PlanetAPI star, float dist_ratio_min, float dist_ratio_max )
	{
		FactoryAPI factory = Global.getFactory();
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		JumpPointAPI jump_point = factory.createJumpPoint( star.getFullName()+" Gravity Well" );
		JumpDestination destination = new JumpDestination( star, star.getFullName() );
		destination.setMinDistFromToken( dist_ratio_min * star.getRadius() );
		destination.setMaxDistFromToken( dist_ratio_max * star.getRadius() );
		jump_point.addDestination( destination );
		jump_point.setStandardWormholeToStarOrPlanetVisual( star );
		//jump_point.setDestinationVisual(null, null, system_root);
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
	
	
	// this ratio is an observed ratio between the distances from Corvus to its third planet
	// in hyperspace vs. normal space
	private static final float hyperspace_compression = 0.0612f; // (459f / 7500f) // in pixels at default zoom
	
	private void update_hyperspace_jump_point_location( JumpPointAPI jump_point, StarSystemAPI system, SectorEntityToken system_root, SectorEntityToken system_entity )
	{
		Vector2f system_location = new Vector2f( system.getLocation() );
		Vector2f local_entity_absolute_location = 
		  calculate_absolute_location( system_root, system_entity, hyperspace_compression );

		jump_point.getLocation().set( 
		  system_location.x + local_entity_absolute_location.x,
		  system_location.y + local_entity_absolute_location.y );
	}
	
	// resolves orbits of an entity until the given root object is encountered
	// if entity does not orbit around anything relative to the root, the result is undefined
	private Vector2f calculate_absolute_location( SectorEntityToken root, SectorEntityToken entity, float scale )
	{
		Vector2f location = new Vector2f();
		if( root == null || entity == null )
			return location;
		// loop through the orbital foci until the root is reached
		SectorEntityToken cursor = entity;
		while( cursor != null && cursor != root )
		{
			Vector2f cursor_location = cursor.getLocation();
			if( cursor_location == null )
				return location;
			location.translate( 
			  scale * cursor_location.x, 
			  scale * cursor_location.y );
			OrbitAPI orbit = cursor.getOrbit();
			if( orbit == null )
				return location;
			cursor = orbit.getFocus();
		}
		return location;
	}
	
}
