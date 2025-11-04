package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import data.scripts.plugins.TheNomadsCombatEnginePlugin;
import data.scripts.world.systems.TheNomadsNur;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

public class TheNomadsModPlugin extends BaseModPlugin {

    private boolean addedPlugin = false;

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
        new TheNomadsNur(true).generate(sector);
    }

    public void advance(float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (engine != null && !addedPlugin) {
            engine.addPlugin(new TheNomadsCombatEnginePlugin());
            addedPlugin = true;
        }

        // Reset flag when combat ends so it adds plugin again next time
        if (engine == null) {
            addedPlugin = false;
        }
    }
}
