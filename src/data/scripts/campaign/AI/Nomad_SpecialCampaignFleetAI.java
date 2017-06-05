package src.data.scripts.campaign.AI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;

import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventManagerAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;

import com.fs.starfarer.api.impl.campaign.ids.Events;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import src.data.scripts.campaign.Nomad_CampaignArmada;
import src.data.scripts.campaign.Nomad_SpecialFactory;

public class Nomad_SpecialCampaignFleetAI implements Script {

    private SectorEntityToken hideoutLocation;
    private final SectorEntityToken home;
    private Nomad_CampaignArmada armada;

    public Nomad_SpecialCampaignFleetAI(Nomad_CampaignArmada armada) {
        this.armada = armada;
        this.home = this.armada.getHome();

    }

    public void init() {
        LocationAPI location = this.home.getContainingLocation();
        CampaignFleetAPI chief = this.armada.getLeaderFleet();
        location.addEntity(chief);

        chief.setLocation(this.home.getLocation().x - 500, this.home.getLocation().y + 500);
       // Global.getSector().getCampaignUI().addMessage("Launch:Go on 2 " + this.home.getId());
        chief.addAssignment(FleetAssignment.ORBIT_PASSIVE, this.home, 5f, this);

        Nomad_SpecialFactory factory = armada.getFactory();

        this.armada.setEscortFleets(factory.spawnRoyalCommandFleet(chief), 0);
        this.armada.setEscortFleets(factory.spawnScoutFleet(chief), 1);
        this.armada.setEscortFleets(factory.spawnScoutFleet(chief), 2);

        Nomad_Escort aiescort = new Nomad_Escort(this.armada);
        aiescort.run(hideoutLocation, 5);
    }

    @Override
    public void run() {
        pickLocation(this.armada.getLeaderFleet());
        if (this.hideoutLocation == null) {
            this.hideoutLocation = Global.getSector().getEntityById("stationNom1");
        }
        int duration = 10;
     //   Global.getSector().getCampaignUI().addMessage("Run:Go on " + hideoutLocation.getId());
        this.armada.getLeaderFleet().getAI().doNotAttack(Global.getSector().getPlayerFleet(), 10000f);
        this.armada.getLeaderFleet().addAssignment(FleetAssignment.GO_TO_LOCATION, hideoutLocation, 10000f, this);
        this.armada.getLeaderFleet().addAssignment(FleetAssignment.ORBIT_PASSIVE, hideoutLocation, duration, this);

        Nomad_Escort aiescort = new Nomad_Escort(this.armada);
        aiescort.run(hideoutLocation, duration);
    }

    private void pickLocation(CampaignFleetAPI fleet) {
        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>();

        CampaignEventManagerAPI eventManager = Global.getSector().getEventManager();

        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            //if (market.getFactionId().equals(Factions.PIRATES)) continue;
            if (fleet.getFaction().getRelationship(market.getFaction().getId())<0f) {
                continue;
            }
            if (market.getStarSystem() == null || market.getStarSystem().hasTag(Tags.THEME_REMNANT) || market.getStarSystem().hasTag(Tags.THEME_DERELICT)) {
                continue;
            }
            if (eventManager.isOngoing(new CampaignEventTarget(market), Events.PERSON_BOUNTY)) {
                continue;
            }
            float dist = Misc.getDistance(market.getLocationInHyperspace(), fleet.getLocationInHyperspace());

            float weight = Math.max(0, 500000f - dist);

            picker.add(market, weight);
        }
        MarketAPI market = picker.pick();
        if (market == null) {
            hideoutLocation = this.home;
            return;
        }
        StarSystemAPI system = market.getStarSystem();
        if (system != null) {
            WeightedRandomPicker<SectorEntityToken> picker2 = new WeightedRandomPicker<SectorEntityToken>();
            for (SectorEntityToken planet : system.getPlanets()) {
                if (planet.isStar()) {
                    continue;
                }
                picker2.add(planet);
            }
            hideoutLocation = picker2.pick();
        }
    }
    /*
    private void pickLocationold() {
        WeightedRandomPicker<StarSystemAPI> systemPicker = new WeightedRandomPicker<StarSystemAPI>();

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            float weight = system.getPlanets().size();
            float mult = 0f;

            if (system.hasPulsar()) {
                continue;
            }

            if (system.hasTag(Tags.THEME_MISC_SKIP)) {
                mult = 1f;
            }
            if (system.hasTag(Tags.THEME_RUINS_SECONDARY)) {
                mult = 1f;
            }
            if (system.hasTag(Tags.THEME_RUINS_MAIN)) {
                mult = 1f;
            }
            if (system.hasTag(Tags.THEME_DERELICT)) {
                mult = 0f;
            }
            if (system.hasTag(Tags.THEME_REMNANT)) {
                mult = 0f;
            }
            if (system.) else if (system.hasTag(Tags.THEME_MISC)) {
                mult = 3f;
            } else if (system.hasTag(Tags.THEME_RUINS)) {
                mult = 7f;
            } else if (system.hasTag(Tags.THEME_REMNANT_DESTROYED)) {
                mult = 3f;
            }  {
                if (mult <= 0) {
                    continue;
                }
            }
            float dist = system.getLocation().length();
            float distMult = Math.max(0, 500000f - dist);

            systemPicker.add(system, weight * mult * distMult);
        }

        StarSystemAPI system = systemPicker.pick();

        if (system != null) {
            WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>();
            for (SectorEntityToken planet : system.getPlanets()) {
                if (planet.isStar()) {
                    continue;
                }
                picker.add(planet);
            }
            hideoutLocation = picker.pick();
        }

    }*/
 /*
    private void pickLocation() {
        WeightedRandomPicker<MarketAPI> systemPicker = new WeightedRandomPicker<MarketAPI>();
        List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
        for (MarketAPI market : markets) {
            if (market.getFactionId() != "nomads") {
                if (market.getFaction().getRelationship("nomads") < 0) {
                    continue;
                }
            }
            float weight = market.getSize();
            float mult = 0f;
            float dist = market.getStarSystem().getLocation().length();
            float distMult = Math.max(0, 100000f - dist);

            systemPicker.add(market, weight * mult * distMult);
        }

        MarketAPI market = systemPicker.pick();
        if (market == null) {
            hideoutLocation = this.home;
        }
        else hideoutLocation = market.getPrimaryEntity();

    }*/

}
