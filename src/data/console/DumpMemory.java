package data.console;

import java.lang.StringBuilder;
import java.util.LinkedList;
import java.util.List;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;


public class DumpMemory implements BaseCommand {

  @Override
  public CommandResult runCommand(String args, CommandContext context) {
    if (context == CommandContext.CAMPAIGN_MAP) {
      //
      StringBuilder output = new StringBuilder();
      MemoryAPI mem = null;
      //
      if ("character".equalsIgnoreCase(args.trim())
      ||  "char".equalsIgnoreCase(args.trim()) )
      {
        mem = Global.getSector().getCharacterData().getMemory();
      } else if ("fleet".equalsIgnoreCase(args.trim())) {
        mem = Global.getSector().getPlayerFleet().getMemory();
      } else {
        return CommandResult.BAD_SYNTAX;
      }
      //
      if (mem != null) {
        for (String key : mem.getKeys()) {
          Object val = mem.get(key);
          output.append("  ").append(key).append(": ").append(String.valueOf(val)).append("\n");
        }
      }
      String out = output.toString();
      if (out.length() > 0)
        Console.showMessage( out );
      //
      return CommandResult.SUCCESS;
    } else {
      return CommandResult.WRONG_CONTEXT;
    }
  }
  
}
