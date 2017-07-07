package data.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersistentUIDataAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.List;
import java.util.Map;


public class AddAbilityFirstFreeSlot extends BaseCommandPlugin
{
  @Override
  public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap)
  {
		if (dialog == null)
      return false;
    
    // grant ability if they don't already have it
    CharacterDataAPI chr = Global.getSector().getCharacterData();
		String abilityId = params.get(0).getString(memoryMap);
		if (!chr.getAbilities().contains(abilityId)) {
      // add it
      chr.addAbility(abilityId);
      AddRemoveCommodity.addAbilityGainText(abilityId, dialog.getTextPanel());

      // assign new ability to first free slot
      PersistentUIDataAPI.AbilitySlotsAPI slots = Global.getSector().getUIData().getAbilitySlotsAPI();
      int player_active_bar_idx = slots.getCurrBarIndex();
      boolean found = false;
      for (int b = 0; b < 5; ++b)
      {
        slots.setCurrBarIndex(b);
        for (PersistentUIDataAPI.AbilitySlotAPI slot : slots.getCurrSlotsCopy())
        {
          String slotAbilityId = slot.getAbilityId();
          if (slotAbilityId == null || "".equals(slotAbilityId)) {
            slot.setAbilityId( abilityId );
            found = true;
            break;
          }
        }
        if (found)
          break;
      }
      slots.setCurrBarIndex(player_active_bar_idx);      
    }
		// end
		return true;
  }
}
