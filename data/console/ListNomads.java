package data.console;

import java.lang.StringBuilder;
import java.util.LinkedList;
import java.util.List;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;


public class ListNomads implements BaseCommand{

  @Override
  public CommandResult runCommand(String args, CommandContext context) {
    if (context == CommandContext.CAMPAIGN_MAP) {
      //
      List<CampaignFleetAPI> nomad_fleets = new LinkedList<CampaignFleetAPI>();
      Map<LocationAPI,Integer> nomad_fleet_counts_by_location = new HashMap<LocationAPI,Integer>();
      int oasis_ship_counts = 0;
      //
      for (LocationAPI location : Global.getSector().getAllLocations()) {
        for (CampaignFleetAPI fleet : location.getFleets()) {
          if ("nomads".equals(fleet.getFaction().getId())) {
            nomad_fleets.add(fleet);
            Integer count = nomad_fleet_counts_by_location.get(location);
            nomad_fleet_counts_by_location.put(location, 1 + (count == null? 0 : count));
            for (FleetMemberAPI fleet_member : fleet.getMembersWithFightersCopy()) {
              if ("nom_oasis".equals(fleet_member.getHullId())) {
                oasis_ship_counts += 1;
              }
            }
          }
        }
      }
      //
      StringBuilder output = new StringBuilder();
      for (Map.Entry<LocationAPI, Integer> entry : nomad_fleet_counts_by_location.entrySet()) {
        output.append(entry.getKey());
        output.append(":");
        output.append(entry.getValue());
        output.append(", ");
      }
      Console.showMessage(output.toString());
      //
      return CommandResult.SUCCESS;
      //
    } else {
      return CommandResult.WRONG_CONTEXT;
    }
  }
  
}
