package data.scripts.plugins;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEnginePlugin;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import java.util.Iterator;
import java.util.List;

public class TheNomadsRoadrunnerEngineFXPlugin implements CombatEnginePlugin, EveryFrameCombatPlugin
{
	private CombatEngineAPI engine;
	
	
	public void init( CombatEngineAPI engine )
	{
		this.engine = engine;
	}

	public void advance( float amount, List events )
	{
		if( engine.isPaused() )
			return;
		
		/*for( Iterator i = engine.getShips().iterator(); i.hasNext(); )
		{
			ShipAPI ship = (ShipAPI) i.next();
			
		}*/
	}
}
