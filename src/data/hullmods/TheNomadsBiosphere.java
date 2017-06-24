package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.BaseHullMod;


public class TheNomadsBiosphere extends BaseHullMod
{
	@Override
	public void applyEffectsBeforeShipCreation( HullSize hullSize, MutableShipStatsAPI stats, String id )
  {
		stats.getSuppliesPerMonth().modifyFlat( id, 0.25f );
	}

	@Override
	public boolean isApplicableToShip( ShipAPI ship )
	{
		// Oasis only in reality
		return ship.getHullSpec().getHullId().equals( "nom_oasis" );
	}
}
