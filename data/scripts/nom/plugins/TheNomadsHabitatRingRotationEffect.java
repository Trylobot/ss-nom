package data.scripts.nom.plugins;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class TheNomadsHabitatRingRotationEffect implements EveryFrameWeaponEffectPlugin
{
	private float direction = 1; //Math.signum((float) Math.random() - 0.5f);
	
	public void advance( float amount, CombatEngineAPI engine, WeaponAPI weapon )
	{
		if( engine.isPaused() )
			return;
		
		float result_angle = weapon.getCurrAngle();
		
		result_angle += direction * amount * 10f;
		float firing_arc = weapon.getArc();
		float weapon_facing_angle = weapon.getArcFacing() + (weapon.getShip() != null ? weapon.getShip().getFacing() : 0);
		if( !isBetween( weapon_facing_angle - firing_arc/2, weapon_facing_angle + firing_arc/2, result_angle ))
			direction = -direction;

		weapon.setCurrAngle( result_angle );
	}

	public static boolean isBetween( float one, float two, float check )
	{
		one = normalizeAngle( one );
		two = normalizeAngle( two );
		check = normalizeAngle( check );
		if( check >= one && check <= two ) return true;
		if( one > two ) {
			if( check <= two ) return true;
			if( check >= one ) return true;
		}
		return false;
	}
	
	public static float normalizeAngle( float angleDeg )
	{
		return (angleDeg % 360f + 360f) % 360f;
	}
}
