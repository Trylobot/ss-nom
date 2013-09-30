package data.scripts;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import data.scripts.nom.world.Nur;
// import data.scripts.nom.plugins.TheNomadsCharacterCreationPlugin;

public class TheNomadsModPlugin extends BaseModPlugin
{
	@Override
	public void onNewGame()
	{
		init();
	}

	private static void init()
	{
		SectorAPI sector = Global.getSector();
		new Nur().generate( sector );
	}
}
