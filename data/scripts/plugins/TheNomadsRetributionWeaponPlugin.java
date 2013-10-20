package data.scripts.plugins;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEnginePlugin;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.misc.Utils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.lwjgl.util.vector.Vector2f;

public class TheNomadsRetributionWeaponPlugin implements CombatEnginePlugin, EveryFrameCombatPlugin
{
	// "fang" refers to the Nomad Bomber ship, the "Fang", which launches Retribution missiles after dying.	
	private static final float RETRIBUTION_LAUNCH_TIMER = 3.0f;
	private static final float RETRIBUTION_ARM_DISTANCE_SQUARED = 35.0f * 35.0f;
	
	private CombatEngineAPI engine = null;
	private HashMap fang_hulks = new HashMap();
	private HashMap unarmed_retribution_missiles = new HashMap();
	private IntervalUtil interval = new IntervalUtil( 0.5f, 1.5f );
	private float clock = 0.0f;
	
	public void init( CombatEngineAPI engine )
	{
		this.engine = engine;
	}

	public void advance( float amount, List events )
	{
		clock += amount; // for fang launch timings
		interval.advance( amount );
		if( !interval.intervalElapsed() )
			return;
		
		// find new hulked fangs
		for( Iterator i = engine.getShips().iterator(); i.hasNext(); )
		{
			ShipAPI ship = (ShipAPI) i.next();
			if( fang_hulks.containsKey( ship ))
				continue; // already know about this one
			if( ship.isHulk() && "nom_fang".equals( ship.getHullSpec().getHullId() ))
				fang_hulks.put( ship, new Float( clock )); // mark time 
		}
		// check timers on known hulks
		for( Iterator i = fang_hulks.entrySet().iterator(); i.hasNext(); )
		{
			Entry entry = (Entry) i.next();
			ShipAPI fang_hulk = (ShipAPI) entry.getKey();
			// check for hulk complete destruction during timer duration
			if( !engine.isEntityInPlay( fang_hulk ))
			{
				i.remove();
				continue; // skip rest
			}
			Float found_clock_time = (Float) entry.getValue();
			// if timer is elapsed, perform the launch actions.
			if( clock >= found_clock_time + RETRIBUTION_LAUNCH_TIMER )
			{
				// swap ship sprite for the version without the missile
				fang_hulk.setSprite( "nomads", "nom_fang_empty" );
				// create the missile as if it had been launched from the ship
				MissileAPI missile = (MissileAPI) engine.spawnProjectile( 
				  fang_hulk, null, "nom_retribution_postmortem_launcher", 
				  fang_hulk.getLocation(), fang_hulk.getFacing(), fang_hulk.getVelocity() );
				missile.setAngularVelocity( fang_hulk.getAngularVelocity() );
				missile.setCollisionClass( CollisionClass.NONE );
				// update entity trackers
				unarmed_retribution_missiles.put( missile, fang_hulk );
				entry.setValue( Float.MAX_VALUE ); // poison entry to prevent future launches
			}
		}
		// check timers on unarmed retribution missiles
		for( Iterator i = unarmed_retribution_missiles.entrySet().iterator(); i.hasNext(); )
		{
			Entry entry = (Entry) i.next();
			MissileAPI missile = (MissileAPI) entry.getKey();
			ShipAPI launching_hulk = (ShipAPI) entry.getValue();
			{
				if( Utils.get_distance_squared( missile.getLocation(), launching_hulk.getLocation() ) >= RETRIBUTION_ARM_DISTANCE_SQUARED )
				{
					// set the collision class of the missile so that it can collide with things again
					missile.setCollisionClass( CollisionClass.MISSILE_NO_FF );
					i.remove();
				}
			}
		}
	}
}
