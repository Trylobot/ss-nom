/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.data.scripts.campaign.AI;

import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import src.data.scripts.campaign.Nomad_CampaignArmada;

public class Nomad_Escort {

    private Nomad_CampaignArmada armada;

    public Nomad_Escort(Nomad_CampaignArmada armada) {
        this.armada = armada;
    }

    public void run(SectorEntityToken location, int duration) {
        for (int i = 0; i < this.armada.getEscortFleets().length; i++) {
            this.armada.getEscortFleets()[i].clearAssignments();
            this.armada.getEscortFleets()[i].addAssignment(FleetAssignment.GO_TO_LOCATION, location, 10000f);
            if (i == 0) {
                this.armada.getEscortFleets()[i].addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, location, duration);
            }
            this.armada.getEscortFleets()[i].addAssignment(FleetAssignment.PATROL_SYSTEM, location, duration);
        }
    }
/*
    private void scoutRun(SectorEntityToken location) {
        for (int i = 0; i < this.armada.getEscortFleets().length; i++) {
            this.armada.getEscortFleets()[i].clearAssignments();
            this.armada.getEscortFleets()[i].addAssignment(FleetAssignment.GO_TO_LOCATION, location, 10000f);
            if (i == 0) {
                this.armada.getEscortFleets()[i].addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, location, 10f);
            }
            this.armada.getEscortFleets()[i].addAssignment(FleetAssignment.PATROL_SYSTEM, location, 10f);
        }
    }

    private void royalRun(SectorEntityToken location) {

    }
*/
    /*
    @Override
    public void run() {

        CampaignFleetAPI fleet = armada.getEscortFleets()[this.identity];
        CampaignFleetAPI chief = armada.getLeaderFleet();
        if (chief == null) {
            if (!armada.isGoDespawn()) {
                armada.despawn();
            }
            Global.getSector().getCampaignUI().addMessage("null chief");

            return;
        }
        if (chief.getCurrentAssignment() == null) {
            Global.getSector().getCampaignUI().addMessage("ORBIT_AGGRESSIVE" + this.identity);
            fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, armada.getLeaderFleet(), 2f, this);
            fleet.setCircularOrbitAngle(chief.getFacing()+1.57f);
        }

        if (chief.getCurrentAssignment().getAssignment().equals(FleetAssignment.ORBIT_PASSIVE)) {
            if (identity >= 4) {
                Global.getSector().getCampaignUI().addMessage("PATROL_SYSTEM" + this.identity);
                fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, armada.getLeaderFleet(), 2f, this);
            } else {
                Global.getSector().getCampaignUI().addMessage("ORBIT_AGGRESSIVE" + this.identity);
                fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, armada.getLeaderFleet(), 2f, this);
            }
        }
        if (chief.getCurrentAssignment().getAssignment().equals(FleetAssignment.PATROL_SYSTEM)) {
            Global.getSector().getCampaignUI().addMessage("ORBIT_AGGRESSIVE" + this.identity);
            fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, armada.getLeaderFleet(), 2f, this);
            fleet.setCircularOrbitAngle(chief.getFacing()+1.57f);
        }
    }
     */
}
