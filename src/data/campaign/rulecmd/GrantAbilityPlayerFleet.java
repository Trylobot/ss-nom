package data.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.List;
import java.util.Map;


public class GrantAbilityPlayerFleet extends BaseCommandPlugin {

  @Override
  public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
    //
    String abilityID = params.get(0).string;
    String abilityName = Global.getSettings().getAbilitySpec(abilityID).getName();
    //Color color = new Color(234,214,124,255);
    //
    Global.getSector().getCharacterData().addAbility(abilityID);
    Global.getSector().getCampaignUI().addMessage("You have been granted: "+abilityName+"!"/*, color */);
    //
    return true;
  }

}
