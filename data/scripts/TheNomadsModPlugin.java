package data.scripts;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
// mod scripts
import data.scripts.world.GenericWaypointArmadaController;
import data.scripts.nom.world.Nur;
import data.scripts.nom.plugins.TheNomadsHabitatRingRotationEffect;
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
