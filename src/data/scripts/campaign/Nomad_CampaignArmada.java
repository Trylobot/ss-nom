package src.data.scripts.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import java.util.Random;

public class Nomad_CampaignArmada {

    private CampaignFleetAPI leaderFleet;
    private final CampaignFleetAPI[] escortFleet;
    private SectorEntityToken home;
    private boolean goDespawn;
    private final Nomad_SpecialFactory factory;
    private static final int ROYALC = 0;
    private static final int SCOUT1 = 1;
    private static final int SCOUT2 = 2;

    public Nomad_CampaignArmada(int sizeEscort) {
        this.factory = new Nomad_SpecialFactory();
        this.escortFleet = new CampaignFleetAPI[sizeEscort];
        this.home = Global.getSector().getEntityById("stationnom1");
        if (this.home == null) {
            this.home = Global.getSector().getEntitiesWithTag(Tags.COMM_RELAY).get(0);
        }
        goDespawn = false;
    }

    public SectorEntityToken getHome() {
        return this.home;
    }

    public Nomad_SpecialFactory getFactory() {
        return this.factory;
    }

    public CampaignFleetAPI getLeaderFleet() {
        return this.leaderFleet;
    }

    public void setLeaderFleet(CampaignFleetAPI leaderFleet) {
        this.leaderFleet = leaderFleet;
    }

    public CampaignFleetAPI[] getEscortFleets() {
        return this.escortFleet;
    }

    public void setEscortFleets(CampaignFleetAPI fleet, int index) {
        if (index >= this.escortFleet.length) {
            return;
        }

        this.escortFleet[index] = fleet;
    }

    public void despawn() {
        if (this.goDespawn) {
            return;
        }

       // Global.getSector().getCampaignUI().addMessage("despawn");

        if (home == null) {
            if (this.leaderFleet != null) {
                this.leaderFleet.despawn();
            }
            for (CampaignFleetAPI escortFleet1 : this.escortFleet) {
                if (escortFleet1 != null) {
                    escortFleet1.despawn();
                }
            }
        }
        if (this.leaderFleet != null) {
            this.leaderFleet.clearAssignments();
            this.leaderFleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, this.home, 10000f);
        }
        for (CampaignFleetAPI escortFleet1 : this.escortFleet) {
            if (escortFleet1 != null) {
                escortFleet1.clearAssignments();
                escortFleet1.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, this.home, 10000f);
            }
        }
        this.goDespawn = true;
    }

    public boolean isGoDespawn() {

        return this.goDespawn;
    }

    public boolean isDespawn() {
        int despawn = 0;
        if (this.leaderFleet == null) {
            despawn++;
        } else if (!this.leaderFleet.isAlive()) {
            this.leaderFleet = null;
            despawn++;
        }
        for (CampaignFleetAPI escortFleet1 : this.escortFleet) {
            if (escortFleet1 == null) {
                despawn++;
            } else if (!escortFleet1.isAlive()) {
                escortFleet1 = null;
                despawn++;
            }
        }

        if (despawn == (1 + this.escortFleet.length)) {
            this.goDespawn = false;
            return true;
        } else {
            return false;
        }
    }

    public int isEscortNull() {

        if (under_isScout(SCOUT1)) {
            return SCOUT1;
        }
        if (under_isScout(SCOUT2)) {
            return SCOUT2;
        }
        if (under_isRoyalC(ROYALC)) {
            // Royal do not respawn always each three days, contrary to scout
            int rand = new Random().nextInt(4);
            if (rand == 0) {
                return ROYALC;
            }
        }
        return -1;
    }

    private boolean under_isScout(int index) {
        return (escortFleet[index] == null || !escortFleet[index].getFlagship().getHullId().equals("nom_flycatcher"));
    }

    private boolean under_isRoyalC(int index) {
        return (escortFleet[index] == null || !escortFleet[index].getFlagship().getHullId().equals("nom_gila_monster"));
    }

    public void respawnEscort(int index) {
       // Global.getSector().getCampaignUI().addMessage("Respawn " + index);

        if (index == SCOUT1 || index == SCOUT2) {
            if (this.escortFleet[index] != null) {
                this.escortFleet[index].clearAssignments();
                this.escortFleet[index].addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, this.home, 10000f);
            }

            this.setEscortFleets(factory.spawnScoutFleet(this.leaderFleet), index);

            SectorEntityToken goOn;
            if (this.leaderFleet.getCurrentAssignment() != null) {
                goOn = this.leaderFleet.getCurrentAssignment().getTarget();
            } else {
                goOn = this.home;
            }

            this.escortFleet[index].addAssignment(FleetAssignment.GO_TO_LOCATION, goOn, 10000f);
            this.escortFleet[index].addAssignment(FleetAssignment.PATROL_SYSTEM, goOn, 20);
        }
        if (index == ROYALC) {
            if (this.escortFleet[index] != null) {
                this.escortFleet[index].clearAssignments();
                this.escortFleet[index].addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, this.home, 10000f);
            }

            this.setEscortFleets(factory.spawnRoyalCommandFleet(this.home), index);

            SectorEntityToken goOn;
            if (this.leaderFleet.getCurrentAssignment() != null) {
                goOn = this.leaderFleet.getCurrentAssignment().getTarget();
            } else {
                goOn = this.home;
            }

            this.escortFleet[index].addAssignment(FleetAssignment.GO_TO_LOCATION, goOn, 10000f);
            this.escortFleet[index].addAssignment(FleetAssignment.PATROL_SYSTEM, goOn, 20);
        }
    }


    /*
    public boolean isEscortAlive(int index) {
        if (index >= this.escortFleet.length) {
            return true;
        }

        return this.escortFleet[index].isAlive();
    }
    
     */
}
