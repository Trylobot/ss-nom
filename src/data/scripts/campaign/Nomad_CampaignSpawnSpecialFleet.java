package src.data.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.Iterator;
import src.data.scripts.campaign.AI.Nomad_SpecialCampaignFleetAI;

public class Nomad_CampaignSpawnSpecialFleet implements EveryFrameScript {

    private final Nomad_CampaignArmada armada;
    private final IntervalUtil timerrespawn;
    private boolean oneTime = false;
    private int cooldown = 0;

    public Nomad_CampaignSpawnSpecialFleet() {

        timerrespawn = new IntervalUtil(3, 3);
        armada = new Nomad_CampaignArmada(3);
        oneTime = false;

    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        SectorAPI sector = Global.getSector();
        if (Global.getCurrentState() != GameState.CAMPAIGN) {
            return;
        }
        if (sector.isPaused()) {
            return;
        }
        if (!oneTime) {
            oneTime = true;
            spawnFleet();
        }
        this.timerrespawn.advance(Global.getSector().getClock().convertToDays(amount));

        if (this.timerrespawn.intervalElapsed()) {

            if (armada.isDespawn()) {
                if (cooldown > 30) {
                    cooldown = 0;
                    retireOasis();
                    spawnFleet();
                }

                cooldown++;
            } else if (armada.getLeaderFleet() == null || !armada.getLeaderFleet().isAlive() || !armada.getLeaderFleet().getFlagship().getHullId().equals("nom_oasis")) {

                insertOasis();
                armada.despawn();
            } else if (!armada.isGoDespawn()) {
                int escortalive = armada.isEscortNull();
                if (escortalive != -1) {
                    armada.respawnEscort(escortalive);
                }
            }
        }

    }

    private void spawnFleet() {
        armada.setLeaderFleet(armada.getFactory().spawnFleet());
        Nomad_SpecialCampaignFleetAI nomad_scriptFleet = new Nomad_SpecialCampaignFleetAI(armada);
        nomad_scriptFleet.init();
    }

    private void insertOasis() {
        SectorEntityToken station = Global.getSector().getEntityById("stationNom1");
        if (station == null) {
            return;
        }

        // add no more than one Oasis
        int count = 0; // first count oasis ships (player could have bought one previously and sold it back)
        FleetDataAPI station_ships = station.getCargo().getMothballedShips();
        for (Iterator i = station_ships.getMembersInPriorityOrder().iterator(); i.hasNext();) {
            FleetMemberAPI ship = (FleetMemberAPI) i.next();
            if ("nom_oasis".equals(ship.getHullId())) {
                ++count;
            }
        }
        if (count == 0) {
            station_ships.addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "nom_oasis_standard"));
        }

    }

    private void retireOasis() {
        SectorEntityToken station = Global.getSector().getEntityById("stationNom1");
        if (station == null) {
            return;
        }
        // remove all Oasis hulls, there's only supposed to be one, and it's cruising around.
        FleetDataAPI station_ships = station.getCargo().getMothballedShips();
        for (Iterator i = station_ships.getMembersInPriorityOrder().iterator(); i.hasNext();) {
            FleetMemberAPI ship = (FleetMemberAPI) i.next();
            if ("nom_oasis".equals(ship.getHullId())) {
                station_ships.removeFleetMember(ship);

            }
        }

    }
}
