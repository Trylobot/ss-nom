package data.scripts;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import data.scripts.trylobot._;
import data.scripts.world.systems.TheNomadsNur;

public class TheNomadsModPlugin extends BaseModPlugin
{
	@Override
	public void onNewGame()
	{
		init();
	}
	
	//@Override
	//public void onGameLoad()
	//{
	//  // does not work
	//	init();
	//}
	
	private void init()
	{
		if( _.can_be_loaded( "data.scripts.world.ExerelinGen" ))
			return;
		
		// normal stand-alone mode
		SectorAPI sector = Global.getSector();
		new TheNomadsNur().generate( sector );
	}
	
}
