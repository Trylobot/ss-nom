package data.hullmods;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class TheNomadsBiosphere extends BaseHullMod
{
	public void applyEffectsBeforeShipCreation( HullSize hullSize, MutableShipStatsAPI stats, String id )
	{
		if( hullSize != HullSize.CAPITAL_SHIP )
			return;

		stats.getBaseSupplyUsePerDay().modifyFlat( id, 0.25f );
	}
}
