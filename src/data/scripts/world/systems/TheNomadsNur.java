package src.data.scripts.world.systems;

import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;


public class TheNomadsNur implements SectorGeneratorPlugin {

    private SectorEntityToken station;

    private static final float star_jump_dist_factor_min = 0.8f;
    private static final float star_jump_dist_factor_max = 1.2f;

    private FactoryAPI factory;

    @Override
    public void generate(SectorAPI sector) {

        this.factory = Global.getFactory();
        StarSystemAPI system = sector.createStarSystem("Nur");
        system.setType(StarSystemType.BINARY_CLOSE);
        system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");

        system.setLightColor(new Color(185, 185, 240)); // light color in entire system, affects all entities
        system.getLocation().set(18000f, -900f);
        LocationAPI hyper = Global.getSector().getHyperspace();

        SectorEntityToken system_center_of_mass = system.initNonStarCenter();
        
       // PlanetAPI star = system.initStar("nur", StarTypes.NEUTRON_STAR, 50f, 40f);

        PlanetAPI starA = system.addPlanet("nur_a", system_center_of_mass, "Nur-A", StarTypes.BLUE_GIANT, 90f, 1000f, 1500f, 30f);
        system.setStar(starA);
        PlanetAPI starB = system.addPlanet("nur_b", system_center_of_mass, "Nur-B", StarTypes.RED_GIANT, 270f, 300f, 600f, 30f);
        system.setSecondary(starB);
        PlanetAPI planet_I = system.addPlanet("nur_c", system_center_of_mass, "Naera", "desert", 45f, 300f, 8000f, 199f);
        system.addPlanet("nur_d", planet_I, "Ixaith", "rocky_unstable", 0f, 60f, 800f, 67f);
        system.addPlanet("nur_e", planet_I, "Ushaise", "rocky_ice", 45f, 45f, 1000f, 120f);
        system.addPlanet("nur_g", planet_I, "Riaze", "barren", 90f, 100f, 1200f, 130f);
        system.addPlanet("nur_g", planet_I, "Riaze-Tremn", "frozen", 135f, 35f, 1500f, 132f);
        PlanetAPI planet_I__moon_e = system.addPlanet("nur_h", planet_I, "Eufariz", "frozen", 180f, 65f, 1750f, 200f);
        system.addPlanet("nur_l", planet_I, "Thumn", "rocky_ice", 225f, 100f, 2000f, 362f);

        // specs
        planet_I.getSpec().setAtmosphereColor(new Color(160, 110, 45, 140));
        planet_I.getSpec().setCloudColor(new Color(255, 255, 255, 23));
        planet_I.getSpec().setTilt(15);
        planet_I.applySpecChanges();

        // stations
        this.station = system.addOrbitalStation("stationnom1", planet_I__moon_e, 180f, 300f, 50, "Naeran Orbital Storage & Resupply", "nomads");
        this.station.setCircularOrbitPointingDown(system.getEntityById("nur_h"), 45, 300, 50);

        // rings & bands
        system.addRingBand(planet_I, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 630f, 30f);

        JumpPointAPI jumpPoint = factory.createJumpPoint("jump_point_alpha", "Jump Point Alpha");
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(system_center_of_mass, 0f, 500f, 30f);
        jumpPoint.setOrbit(orbit);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);
        
        
        system.autogenerateHyperspaceJumpPoints(true,true);
        
        planet_I.setCustomDescriptionId("nom_planet_naera");


    }
    
  

    private JumpPointAPI init_star_gravitywell_jump_point(StarSystemAPI system, SectorEntityToken system_root, PlanetAPI star, float dist_ratio_min, float dist_ratio_max) {

        LocationAPI hyper = Global.getSector().getHyperspace();

        JumpPointAPI jump_point;
        jump_point = factory.createJumpPoint(star.getFullName() + "-gravity-well", star.getFullName() + " Gravity Well");
        JumpDestination destination = new JumpDestination(star, star.getFullName());
        destination.setMinDistFromToken(dist_ratio_min * star.getRadius());
        destination.setMaxDistFromToken(dist_ratio_max * star.getRadius());
        jump_point.addDestination(destination);
        jump_point.setStandardWormholeToStarOrPlanetVisual(star);
        //jump_point.setDestinationVisual(null, null, system_root);
        hyper.addEntity(jump_point);

        update_hyperspace_jump_point_location(jump_point, system, system_root, star);
        return jump_point;
    }


    // this ratio is an observed ratio between the distances from Corvus to its third planet
    // in hyperspace vs. normal space
    private static final float hyperspace_compression = 0.0612f; // (459f / 7500f) // in pixels at default zoom

    private void update_hyperspace_jump_point_location(JumpPointAPI jump_point, StarSystemAPI system, SectorEntityToken system_root, SectorEntityToken system_entity) {
        Vector2f system_location = new Vector2f(system.getLocation());
        Vector2f local_entity_absolute_location
                = calculate_absolute_location(system_root, system_entity, hyperspace_compression);

        jump_point.getLocation().set(
                system_location.x + local_entity_absolute_location.x,
                system_location.y + local_entity_absolute_location.y);
    }

    // resolves orbits of an entity until the given root object is encountered
    // if entity does not orbit around anything relative to the root, the result is undefined
    private Vector2f calculate_absolute_location(SectorEntityToken root, SectorEntityToken entity, float scale) {
        Vector2f location = new Vector2f();
        if (root == null || entity == null) {
            return location;
        }
        // loop through the orbital foci until the root is reached
        SectorEntityToken cursor = entity;
        while (cursor != null && cursor != root) {
            Vector2f cursor_location = cursor.getLocation();
            if (cursor_location == null) {
                return location;
            }
            location.translate(
                    scale * cursor_location.x,
                    scale * cursor_location.y);
            OrbitAPI orbit = cursor.getOrbit();
            if (orbit == null) {
                return location;
            }
            cursor = orbit.getFocus();
        }
        return location;
    }

}
