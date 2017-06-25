package data.scripts.world.systems;

import java.awt.Color;
import java.util.Iterator;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
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
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

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
  private boolean colony_armada_feature_bit = true;
  
  private FactoryAPI factory;
  private SectorEntityToken station;
  
  
  public TheNomadsNur() {
  }
  
  public TheNomadsNur( boolean colony_armada_feature_enabled ) {
    this.colony_armada_feature_bit = colony_armada_feature_enabled;
  }
  
  
	public void generate( SectorAPI sector )
	{
		factory = Global.getFactory();
    // stars, planets, moons
		StarSystemAPI system = sector.createStarSystem( "Nur" );
    system.setLightColor( new Color( 185, 185, 240 )); // light color in entire system, affects all entities
		system.getLocation().set( 18000f, -900f );
		SectorEntityToken system_center_of_mass = system.initNonStarCenter();
    //
    PlanetAPI starA = system.addPlanet("nur_a", system_center_of_mass, "Nur-A", StarTypes.BLUE_GIANT, 90f, 1000f, 1500f, 30f);
    system.setStar(starA);
    system.addCorona(starA, 75f, 0f, 0.05f, 0.0f);
    //
    PlanetAPI starB = system.addPlanet("nur_b", system_center_of_mass, "Nur-B", StarTypes.RED_GIANT, 270f, 300f, 600f, 30f);
    system.setSecondary(starB);
    system.addCorona(starB, 75f, 0f, 0.05f, 0.0f);
    //
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
    PlanetAPI planet_I__moon_c = system.addPlanet("nur_f", planet_I, "Riaze", "barren", 90f, 100f, 1200f, 130f);
    PlanetAPI planet_I__moon_d = system.addPlanet("nur_g", planet_I, "Riaze-Tremn", "frozen", 135f, 35f, 1500f, 132f);
    PlanetAPI planet_I__moon_e = system.addPlanet("nur_h", planet_I, "Eufariz", "frozen", 180f, 65f, 1750f, 200f);
    PlanetAPI planet_I__moon_f = system.addPlanet("nur_i", planet_I, "Thumn", "rocky_ice", 225f, 100f, 2000f, 362f);
    // stations
    station = system.addOrbitalStation("stationnom1", planet_I__moon_e, 180f, 300f, 50f, "Naeran Orbital Storage & Resupply", "nomads");
    station.setCircularOrbitPointingDown(system.getEntityById("nur_h"), 45, 300, 50);
    station.addTag(Tags.STATION);
    // hyperspace
    JumpPointAPI jumpPoint = factory.createJumpPoint("jmp_stationnom1", "Orbital Station Jump Point");
    jumpPoint.setOrbit(Global.getFactory().createCircularOrbit(planet_I__moon_e, 90f, 300f, 50f));
    jumpPoint.setStandardWormholeToHyperspaceVisual();
    system.addEntity(jumpPoint);
    //
    system.autogenerateHyperspaceJumpPoints(true, true);
    
    // relationships
		FactionAPI nomads_faction = sector.getFaction( "nomads" );
		Object[] all_factions = sector.getAllFactions().toArray();
		for( int i = 0; i < all_factions.length; ++i )
		{
			FactionAPI cur_faction = (FactionAPI) all_factions[i];
			if( cur_faction == nomads_faction )
				continue;
			if( "neutral".equals(    cur_faction.getId())
			||  "independent".equals(cur_faction.getId()) ) {
        // neutral and independent are friendly
				nomads_faction.setRelationship( cur_faction.getId(), 1 );
			} else {
        // all other factions are hostiles
				nomads_faction.setRelationship( cur_faction.getId(), -1 );
			}
		}
    // the player is neutral
		nomads_faction.setRelationship( "player", 0 );
		
    // DEPRECATED
    //// armada formation
    //CampaignArmadaEscortFleetPositionerAPI armada_formation =
    //  new CampaignArmadaFormationOrbit(
    //    sector,
    //    300.0f, // orbitRadius
    //    1.0f, // orbitDirection
    //    0.8f // orbitPeriodDays
    //  );
    
		// armada (leader fleet + escorts) controller script
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
		CampaignArmadaController nomad_armada =
			new CampaignArmadaController(
				"nomads", // faction
				"colonyFleet", // leader/VIP fleet
				"nom_oasis", // flagship of flagships
				sector, // global sector api
				planet_I__moon_f, // spawn location
        station.getMarket(), // market
				8, // escort_fleet_count
				escort_pool,
				escort_weights,
				null,
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
    // make sure all Nomad-used hullmods are purchaseable!
    cargo.addItems(CargoItemType.MOD_SPEC, "fluxbreakers", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "fluxcoil", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "unstable_injector", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "recovery_shuttles", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "expanded_deck_crew", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "magazines", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "targetingunit", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "heavyarmor", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "armoredweapons", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "turretgyros", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "blast_doors", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "reinforcedhull", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "autorepair", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "unstable_injector", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "nav_relay", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "missleracks", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "eccm", 1);
    cargo.addItems(CargoItemType.MOD_SPEC, "auxiliarythrusters", 1);
    //
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
		StockDescriptor[] restock = {
      new StockDescriptor("nom_gila_monster_antibattleship", 1, 92.0f),
      new StockDescriptor("nom_sandstorm_assault", 2, 59.0f),
      new StockDescriptor("nom_rattlesnake_assault", 2, 39.0f),
      new StockDescriptor("nom_scorpion_assault", 3, 29.0f),
      new StockDescriptor("nom_komodo_mk2_assault", 2, 25.0f),
      new StockDescriptor("nom_komodo_assault", 4, 21.0f),
      new StockDescriptor("nom_roadrunner_pursuit", 4, 13.0f),
      new StockDescriptor("nom_flycatcher_fang", 1, 15.0f),
      new StockDescriptor("nom_flycatcher_iguana", 1, 15.0f),
      new StockDescriptor("nom_flycatcher_scarab", 1, 15.0f),
      new StockDescriptor("nom_flycatcher_toad", 1, 15.0f),
      new StockDescriptor("nom_yellowjacket_sniper", 2, 29.0f),
      new StockDescriptor("nom_death_bloom_strike", 5, 9.0f),
    };
		TheNomadsNurStationRestocker station_cargo_restocker
      = new TheNomadsNurStationRestocker( restock, station );
		system.addScript( station_cargo_restocker );
	}
	
  @Override
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
