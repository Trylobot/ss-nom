package data.scripts;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import data.scripts.world.systems.TheNomadsNur;
import data.scripts.plugins.TheNomadsCombatEnginePlugin;

public class TheNomadsModPlugin extends BaseModPlugin
{
	@Override
	public void onNewGame()
	{
		init();
	}
	
	private void init()
	{
		if( can_be_loaded( "data.scripts.world.ExerelinGen" ))
			return;
		
		// normal stand-alone mode
		SectorAPI sector = Global.getSector();
		new TheNomadsNur().generate( sector );
	}
	
	private boolean can_be_loaded( String fullyQualifiedClassName )
	{
		try
		{
			 Global.getSettings().getScriptClassLoader().loadClass( fullyQualifiedClassName );
			 return true;
		}
		catch (ClassNotFoundException ex)
		{
			return false;
		}		
	}
}
