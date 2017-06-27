package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.Misc;


public class TheNomadsRoyal extends BaseHullMod
{
	private static final float HANDLING_MULT = 1.10f;
  private static final float WEAPON_RANGE_MULT = 1.10f;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
  {
		// 10% better handling
		stats.getMaxSpeed().modifyMult(id, HANDLING_MULT);
		stats.getAcceleration().modifyMult(id, HANDLING_MULT);
		stats.getDeceleration().modifyMult(id, HANDLING_MULT);
		stats.getMaxTurnRate().modifyMult(id, HANDLING_MULT);
		stats.getTurnAcceleration().modifyMult(id, HANDLING_MULT);
    // 10% better weapon range
    stats.getEnergyWeaponRangeBonus().modifyMult(id, WEAPON_RANGE_MULT);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		//if (index == 0) return Misc.getRoundedValue(ARMOR_BONUS);
		//if (index == 1) return "" + (int) ((1f - HANDLING_MULT) * 100f);
		//if (index == 2) return "" + (int) ((CAPACITY_MULT - 1f) * 100f); 
		return null;
	}

}
