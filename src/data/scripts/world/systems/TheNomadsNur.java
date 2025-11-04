package data.scripts.world.systems;

import java.awt.Color;
import java.util.Iterator;
import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.util.Misc;
import data.scripts.trylobot.TrylobotUtils;
import data.scripts.world.armada.CampaignArmadaController;
import data.scripts.world.armada.CampaignArmadaController.CampaignArmadaControllerEvent;
import data.scripts.world.armada.CampaignArmadaController.CampaignArmadaControllerEventListener;
import data.scripts.world.armada.CampaignArmadaResourceSharingController;


@SuppressWarnings( "unchecked" )
public class TheNomadsNur implements SectorGeneratorPlugin, CampaignArmadaControllerEventListener
{
  private boolean colony_armada_feature_bit = true;
  
  private FactoryAPI factory;
  private SectorEntityToken station;
  
  private final Color factionColor = new Color(234,214,124,255);
  
  
  public TheNomadsNur() {
  }
  
  public TheNomadsNur( boolean colony_armada_feature_enabled ) {
    this.colony_armada_feature_bit = colony_armada_feature_enabled;
  }
  
  
  @Override
	public void generate(SectorAPI sector) {
		factory = Global.getFactory();

		// Create star system "Nur"
		StarSystemAPI system = sector.createStarSystem("Nur");
		system.setLightColor(new Color(185, 185, 240));
		system.getLocation().set(18000f, -900f);

		SectorEntityToken systemCenterOfMass = system.initNonStarCenter();

		// Primary star
		PlanetAPI starA = system.addPlanet(
			"nur_a", systemCenterOfMass, "Nur-A", StarTypes.BLUE_GIANT,
			90f, 1000f, 1500f, 30f
		);
		system.setStar(starA);
		system.addCorona(starA, 300f, 5f, 0f, 1f);

		// Secondary star
		PlanetAPI starB = system.addPlanet(
			"nur_b", systemCenterOfMass, "Nur-B", StarTypes.RED_GIANT,
			270f, 300f, 600f, 30f
		);
		system.setSecondary(starB);
		system.addCorona(starB, 50f, 5f, 0.05f, 0.5f);

		// Main planet "Naera"
		PlanetAPI planetI = system.addPlanet(
			"nur_c", systemCenterOfMass, "Naera", "desert",
			45f, 300f, 8000f, 199f
		);
		system.addRingBand(planetI, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 630f, 30f);
		planetI.setCustomDescriptionId("nom_planet_naera");
		planetI.getSpec().setAtmosphereColor(new Color(160, 110, 45, 140));
		planetI.getSpec().setCloudColor(new Color(255, 255, 255, 23));
		planetI.getSpec().setTilt(15);
		planetI.applySpecChanges();

		// Moons of Naera
		PlanetAPI moonA = system.addPlanet("nur_d", planetI, "Ixaith", "rocky_unstable", 0f, 60f, 800f, 67f);
		PlanetAPI moonB = system.addPlanet("nur_e", planetI, "Ushaise", "rocky_ice", 45f, 45f, 1000f, 120f);
		PlanetAPI moonC = system.addPlanet("nur_f", planetI, "Riaze", "barren", 90f, 100f, 1200f, 130f);
		PlanetAPI moonD = system.addPlanet("nur_g", planetI, "Riaze-Tremn", "frozen", 135f, 35f, 1500f, 132f);
		PlanetAPI moonE = system.addPlanet("nur_h", planetI, "Eufariz", "frozen", 180f, 65f, 1750f, 200f);
		PlanetAPI moonF = system.addPlanet("nur_i", planetI, "Thumn", "rocky_ice", 225f, 100f, 2000f, 362f);

		// Create the station entity
		SectorEntityToken station = system.addCustomEntity(
			"nur_fabricator_station",           // Unique ID for your station
			"Naeran Orbital Storage & Resupply",           // Display name
			"station_side02",                  // Type ID (vanilla type)
			"nomads"                           // Faction ID
		);

		// Set the orbit around the planet
		station.setCircularOrbit(moonE, 180f, 300f, 50f); // angle, radius, orbit days

		// Add station tag so it's recognized by game systems
		station.addTag("station");

		// Add a market
		MarketAPI market = Global.getFactory().createMarket(
			"naera_station_market",
			station.getName(),
			6  // Market size
		);
		market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		market.setPrimaryEntity(station);
		market.setFactionId("nomads");

		// Add conditions and submarkets
		market.addCondition(Conditions.POPULATION_5);
		market.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market.addSubmarket(Submarkets.GENERIC_MILITARY);
		market.addSubmarket(Submarkets.SUBMARKET_STORAGE);


		station.setMarket(market);
		Global.getSector().getEconomy().addMarket(market, true);

		// Auto generate jump points
		system.autogenerateHyperspaceJumpPoints(true, true);

		// Faction relationships
		FactionAPI nomadsFaction = sector.getFaction("nomads");
		for (FactionAPI faction : sector.getAllFactions()) {
			String factionId = faction.getId();
			if ("nomads".equals(factionId) || "independent".equals(factionId)
					|| "scavengers".equals(factionId) || "neutral".equals(factionId)) {
				nomadsFaction.setRelationship(factionId, 1.00f);
			} else if ("pirates".equals(factionId) || "hegemony".equals(factionId)) {
				nomadsFaction.setRelationship(factionId, -0.65f);
			} else {
				nomadsFaction.setRelationship(factionId, 0.00f);
			}
		}

		// Escort Fleet Setup
		String[] escortPool = {
			"scout", "longRangeScout", "battleGroup", "assassin",
			"royalGuard", "jihadFleet", "carrierGroup", "royalCommandFleet"
		};
		int[] escortWeights = {
			220, 200, 230, 185, 175, 125, 200, 100
		};

		CampaignArmadaController nomadArmada = new CampaignArmadaController(
			"nomads",
			"colonyFleet",
			"nom_oasis",
			sector,
			moonF,
			station.getMarket(),
			8,
			escortPool,
			escortWeights,
			500f,
			1,
			6,
			30
		);
		sector.addScript(nomadArmada);
		nomadArmada.addListener(this);

		// Resource sharing controller
		CampaignArmadaResourceSharingController armadaResourcePool = new CampaignArmadaResourceSharingController(
			sector,
			nomadArmada,
			3.0f,   // 3 days of resource usage
			0.10f,  // 10% skeleton crew requirement
			3.0f,   // 3 light-years fuel range
			12.0f,  // 12 days of resource usage (extended)
			0.50f,  // 50% skeleton crew requirement
			20.0f   // 20 light-years fuel range
		);
		sector.addScript(armadaResourcePool);
        
		// restocker script
		StockDescriptor[] restock = {
      //
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "fluxbreakers", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "fluxcoil", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "unstable_injector", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "recovery_shuttles", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "expanded_deck_crew", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "magazines", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "targetingunit", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "heavyarmor", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "armoredweapons", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "turretgyros", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "blast_doors", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "reinforcedhull", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "autorepair", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "unstable_injector", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "nav_relay", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "missleracks", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "eccm", 1, 1f),
      new StockDescriptor(StockDescriptor.HULLMOD_SPEC, "auxiliarythrusters", 1, 1f),
      //
      new StockDescriptor(StockDescriptor.SHIP, "nom_gila_monster_antibattleship", 1, 11f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_queen_bee_attack", 1, 11f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_sandstorm_assault", 2, 8f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_rattlesnake_assault", 3, 8f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_scorpion_royal_vanguard", 1, 3f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_scorpion_assault", 3, 3f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_komodo_p_overdriven", 1, 5f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_komodo_royal_vanguard", 1, 5f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_komodo_mk2_assault", 3, 3f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_komodo_assault", 4, 3f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_roadrunner_pursuit", 4, 3f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_flycatcher_fang", 2, 1f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_flycatcher_iguana", 1, 1f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_flycatcher_ant", 2, 1f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_flycatcher_toad", 1, 1f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_yellowjacket_sniper", 4, 1f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_death_bloom_strike", 2, 1f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_dust_devil_assault", 2, 3f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_wurm_royal_vanguard", 1, 3f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_wurm_assault", 4, 1f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_cobra_personnel", 1, 2f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_tortoise_freighter", 1, 2f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_cactus_tanker", 1, 2f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_willow_salvage", 1, 2f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_dragonfly_tug", 1, 2f),
      new StockDescriptor(StockDescriptor.SHIP, "nom_leaf_probe", 1, 2f),
      //
      new StockDescriptor(StockDescriptor.FIGHTER_LPC, "nom_fang_wing", 2, 1f),
      new StockDescriptor(StockDescriptor.FIGHTER_LPC, "nom_tarantula_wing", 2, 1f),
      new StockDescriptor(StockDescriptor.FIGHTER_LPC, "nom_toad_wing", 2, 1f),
      new StockDescriptor(StockDescriptor.FIGHTER_LPC, "nom_iguana_wing", 2, 1f),
      new StockDescriptor(StockDescriptor.FIGHTER_LPC, "nom_ant_wing", 2, 1f)
    };
		TheNomadsNurStationRestocker station_cargo_restocker
      = new TheNomadsNurStationRestocker( restock, station, Submarkets.GENERIC_MILITARY );
		system.addScript( station_cargo_restocker );
    
    
	}
	
  @Override
	public void handle_event( CampaignArmadaControllerEvent event )
	{
    if (station == null || station.getMarket() == null
    ||  station.getMarket().getSubmarket("open_market") == null)
      return;
    
    SubmarketAPI open_market = station.getMarket().getSubmarket("open_market");
    // Oasis is not in play; put it for sale at the station (yay!)
		if( "NON_EXISTENT".equals( event.controller_state ))
		{
			// add no more than one Oasis
			int count = 0; // first count oasis ships (player could have bought one previously and sold it back)
			FleetDataAPI station_ships = open_market.getCargo().getMothballedShips();
			for( Iterator i = station_ships.getMembersInPriorityOrder().iterator(); i.hasNext(); )
			{
				FleetMemberAPI ship = (FleetMemberAPI)i.next();
				if( "nom_oasis".equals( ship.getHullId() ))
					++count;
			}
			if( count == 0 )
			{
				station_ships.addFleetMember( factory.createFleetMember( FleetMemberType.SHIP, "nom_oasis_standard" ));
			}
		}
		// Oasis is in play; be patient! T_T
		else if( "JOURNEYING_LIKE_A_BOSS".equals( event.controller_state ))
		{
			// remove all Oasis hulls, there's only supposed to be one, and it's cruising around.
			FleetDataAPI station_ships = open_market.getCargo().getMothballedShips();
			for( Iterator i = station_ships.getMembersInPriorityOrder().iterator(); i.hasNext(); )
			{
				FleetMemberAPI ship = (FleetMemberAPI)i.next();
				if( "nom_oasis".equals( ship.getHullId() ))
				{
					station_ships.removeFleetMember( ship );
				}
			}
		}
	}
}
