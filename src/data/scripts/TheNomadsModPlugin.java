package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import data.scripts.world.systems.TheNomadsNur;


public class TheNomadsModPlugin extends BaseModPlugin
{
	@Override
  public void onNewGame() {
    
    // mods incompatible with system scripts
    if (Global.getSettings().getModManager().isModEnabled("nexerelin"))
      return;
    
    // normal initialization
    new TheNomadsNur().generate(Global.getSector());

  }
}
