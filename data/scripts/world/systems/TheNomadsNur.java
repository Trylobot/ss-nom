package data.scripts.world.systems;

import java.awt.Color;
import java.util.Iterator;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV2;

import data.scripts.trylobot.TrylobotUtils;
import data.scripts.world.armada.CampaignArmadaController;
import data.scripts.world.armada.CampaignArmadaController.CampaignArmadaControllerEvent;
import data.scripts.world.armada.CampaignArmadaController.CampaignArmadaControllerEventListener;
import data.scripts.world.armada.CampaignArmadaFormationOrbit;
import data.scripts.world.armada.CampaignArmadaResourceSharingController;
import data.scripts.world.armada.api.CampaignArmadaEscortFleetPositionerAPI;


@SuppressWarnings( "unchecked" )
public class TheNomadsNur implements SectorGeneratorPlugin, CampaignArmadaControllerEventListener
{
  private FactoryAPI factory;
  private SectorEntityToken station;
  
  
	public void generate( SectorAPI sector )
	{
		factory = Global.getFactory();
    // stars, planets, moons
		StarSystemAPI system = sector.createStarSystem( "Nur" );
    system.setLightColor( new Color( 185, 185, 240 )); // light color in entire system, affects all entities
		system.getLocation().set( 18000f, -900f );
		SectorEntityToken system_center_of_mass = system.initNonStarCenter();
    PlanetAPI starA = system.addPlanet("nur_a", system_center_of_mass, "Nur-A", StarTypes.BLUE_GIANT, 90f, 1000f, 1500f, 30f);
    system.setStar(starA);
    PlanetAPI starB = system.addPlanet("nur_b", system_center_of_mass, "Nur-B", StarTypes.RED_GIANT, 270f, 300f, 600f, 30f);
    system.setSecondary(starB);
    // planets
    PlanetAPI planet_I = system.addPlanet("nur_c", system_center_of_mass, "Naera", "desert", 45f, 300f, 8000f, 199f);
    system.addRingBand(planet_I, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 630f, 30f);
    planet_I.setCustomDescriptionId("nom_planet_naera");
    planet_I.getSpec().setAtmosphereColor(new Color(160, 110, 45, 140));
    planet_I.getSpec().setCloudColor(new Color(255, 255, 255, 23));
    planet_I.getSpec().setTilt(15);
    planet_I.applySpecChanges();
    // moons
    PlanetAPI planet_I__moon_a = system.addPlanet("nur_d", planet_I, "Ixaith", "rocky_unstable", 0f, 60f, 800f, 67f);
    PlanetAPI planet_I__moon_b = system.addPlanet("nur_e", planet_I, "Ushaise", "rocky_ice", 45f, 45f, 1000f, 120f);
    PlanetAPI planet_I__moon_c = system.addPlanet("nur_g", planet_I, "Riaze", "barren", 90f, 100f, 1200f, 130f);
    PlanetAPI planet_I__moon_d = system.addPlanet("nur_g", planet_I, "Riaze-Tremn", "frozen", 135f, 35f, 1500f, 132f);
    PlanetAPI planet_I__moon_e = system.addPlanet("nur_h", planet_I, "Eufariz", "frozen", 180f, 65f, 1750f, 200f);
    PlanetAPI planet_I__moon_f = system.addPlanet("nur_l", planet_I, "Thumn", "rocky_ice", 225f, 100f, 2000f, 362f);
    // stations
    station = system.addOrbitalStation("stationnom1", planet_I__moon_e, 180f, 300f, 50, "Naeran Orbital Storage & Resupply", "nomads");
    station.setCircularOrbitPointingDown(system.getEntityById("nur_h"), 45, 300, 50);
    // hyperspace
    JumpPointAPI jumpPoint = factory.createJumpPoint("jump_point_alpha", "Jump Point Alpha");
    OrbitAPI orbit = Global.getFactory().createCircularOrbit(system_center_of_mass, 0f, 500f, 30f);
    jumpPoint.setOrbit(orbit);
    jumpPoint.setStandardWormholeToHyperspaceVisual();
    system.addEntity(jumpPoint);
    system.autogenerateHyperspaceJumpPoints(true, true);
		
    /*
    
		// armada formation
		CampaignArmadaEscortFleetPositionerAPI armada_formation =
			new CampaignArmadaFormationOrbit(
				sector,
				300.0f, // orbitRadius
				1.0f, // orbitDirection
				0.8f // orbitPeriodDays
			);
		// escorts
		String[] escort_pool = { 
			"scout", 
			"longRangeScout", 
			"battleGroup", 
			"assassin", 
			"royalGuard", 
			"jihadFleet", 
			"carrierGroup",
			"royalCommandFleet"
		};
		int[] escort_weights = {    
			220,
			200,
			230,
			185,
			175,
			125,
			100,
			75
		};
    
		// armada waypoint controller script
		CampaignArmadaController nomad_armada =
			new CampaignArmadaController(
				"nomads", // faction
				"colonyFleet", // leader/VIP fleet
				"nom_oasis",
				sector, // global sector api
				planet_I__moon_f,
				8, // escort_fleet_count
				escort_pool,
				escort_weights,
				armada_formation,
				1, // waypoint_per_trip_minimum
				6, // waypoint_per_trip_maximum
				30 // dead_time_days
			);
		sector.addScript( nomad_armada );
		nomad_armada.addListener( this );
		
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
		sector.addScript( armada_resource_pool );
    
    // station cargo
    CargoAPI cargo = station.getCargo();
		FleetDataAPI station_ships = cargo.getMothballedShips();
		//station_ships.addFleetMember( factory.createFleetMember( FleetMemberType.SHIP, "nom_oasis_standard" ));
		station_ships.addFleetMember( factory.createFleetMember( FleetMemberType.SHIP, "nom_komodo_assault" ));
		station_ships.addFleetMember( factory.createFleetMember( FleetMemberType.SHIP, "nom_wurm_assault" ));
		station_ships.addFleetMember( factory.createFleetMember( FleetMemberType.SHIP, "nom_wurm_assault" ));
		station_ships.addFleetMember( factory.createFleetMember( FleetMemberType.FIGHTER_WING, "nom_iguana_wing" ));
		station_ships.addFleetMember( factory.createFleetMember( FleetMemberType.FIGHTER_WING, "nom_scarab_wing" ));
		cargo.addCrew( 1120 );
		cargo.addSupplies( 3000.0f );
		cargo.addFuel( 3000.0f );
    
		// restocker script
		String[] restock_ship_variant_or_wing_ids = { 
			"nom_gila_monster_antibattleship",
			"nom_sandstorm_assault",
			"nom_rattlesnake_assault",
			"nom_scorpion_assault",
			"nom_komodo_mk2_assault",
			"nom_komodo_assault",
			"nom_roadrunner_pursuit",
			"nom_flycatcher_carrier",
			"nom_yellowjacket_sniper",
			"nom_death_bloom_strike",
			"nom_wurm_assault",
			"nom_fang_wing",
			"nom_toad_wing",
			"nom_iguana_wing",
			"nom_scarab_wing"
		};
		FleetMemberType[] restock_ship_types = {       
			FleetMemberType.SHIP,
			FleetMemberType.SHIP,
			FleetMemberType.SHIP,
			FleetMemberType.SHIP,
			FleetMemberType.SHIP,
			FleetMemberType.SHIP,
			FleetMemberType.SHIP,
			FleetMemberType.SHIP,
			FleetMemberType.SHIP,
			FleetMemberType.SHIP,
			FleetMemberType.SHIP,
			FleetMemberType.FIGHTER_WING,
			FleetMemberType.FIGHTER_WING,
			FleetMemberType.FIGHTER_WING,
			FleetMemberType.FIGHTER_WING
		};
		int[] restock_ship_count_cap = {               
			1,
			2,
			2,
			3,
			2,
			4,
			4,
			4,
			2,
			1,
			5,
			3,
			6,
			6,
			8
		};
		float[] restock_ship_wait_days = {             
			92.0f,
			59.0f,
			39.0f,
			29.0f,
			25.0f,
			21.0f,
			13.0f,
			15.0f,
			13.0f,
			29.0f,
			9.0f,
			8.0f,
			5.2f,
			5.0f,
			4.0f
		};
		TheNomadsNurStationRestocker station_cargo_restocker = new TheNomadsNurStationRestocker(
			restock_ship_variant_or_wing_ids,
			restock_ship_types,
			restock_ship_count_cap,
			restock_ship_wait_days,
		    station );
		system.addScript( station_cargo_restocker );

    */
	}
	
	public void handle_event( CampaignArmadaControllerEvent event )
	{
		// Oasis is not in play; put it for sale at the station (yay!)
		if( "NON_EXISTENT".equals( event.controller_state ))
		{
			// add no more than one Oasis
			int count = 0; // first count oasis ships (player could have bought one previously and sold it back)
			FleetDataAPI station_ships = station.getCargo().getMothballedShips();
			for( Iterator i = station_ships.getMembersInPriorityOrder().iterator(); i.hasNext(); )
			{
				FleetMemberAPI ship = (FleetMemberAPI)i.next();
				if( "nom_oasis".equals( ship.getHullId() ))
					++count;
			}
			if( count == 0 )
			{
				station_ships.addFleetMember( factory.createFleetMember( FleetMemberType.SHIP, "nom_oasis_standard" ));
				TrylobotUtils.debug("added OASIS to station cargo");
			}
		}
		// Oasis is in play; be patient! T_T
		else if( "JOURNEYING_LIKE_A_BOSS".equals( event.controller_state ))
		{
			// remove all Oasis hulls, there's only supposed to be one, and it's cruising around.
			FleetDataAPI station_ships = station.getCargo().getMothballedShips();
			for( Iterator i = station_ships.getMembersInPriorityOrder().iterator(); i.hasNext(); )
			{
				FleetMemberAPI ship = (FleetMemberAPI)i.next();
				if( "nom_oasis".equals( ship.getHullId() ))
				{
					station_ships.removeFleetMember( ship );
					TrylobotUtils.debug("removed OASIS from station cargo");
				}
			}
		}
	}
}
