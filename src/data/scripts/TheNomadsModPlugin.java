package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import data.scripts.world.systems.TheNomadsNur;
import data.scripts.trylobot.TrylobotUtils;


public class TheNomadsModPlugin extends BaseModPlugin
{
	@Override
  public void onNewGame() {
    
    // mods incompatible with system scripts
    if (Global.getSettings().getModManager().isModEnabled("nexerelin"))
      return;
    
    // normal initialization
    TrylobotUtils.print("about to generate sector");
    new TheNomadsNur().generate(Global.getSector());
    TrylobotUtils.print("finished generating sector");

  }
}
