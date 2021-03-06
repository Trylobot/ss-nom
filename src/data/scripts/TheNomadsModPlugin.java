package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;
import data.scripts.world.systems.TheNomadsNur;


public class TheNomadsModPlugin extends BaseModPlugin
{
  @Override  
  public void onApplicationLoad() {  
    if (Global.getSettings().getModManager().isModEnabled("shaderLib")) {  
      ShaderLib.init();  
      LightData.readLightDataCSV("data/lights/nom_light_data.csv");  
      TextureData.readTextureDataCSV("data/lights/nom_texture_data.csv");  
    }
  }
  
  @Override
  public void onNewGame() {
    SectorAPI sector = Global.getSector();
    
    // generate Nur star system, and optionally, the oasis armada
    new TheNomadsNur( true ).generate( sector );
    
    
  }
}
