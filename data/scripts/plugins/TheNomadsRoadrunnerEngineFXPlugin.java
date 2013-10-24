package data.scripts.plugins;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEnginePlugin;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.trylobot._;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.lwjgl.util.vector.Vector2f;

public class TheNomadsRoadrunnerEngineFXPlugin implements CombatEnginePlugin, EveryFrameCombatPlugin
{
	private CombatEngineAPI engine;
	private LinkedList tracker = new LinkedList();
	private float accumulator = 0.0f;
	private static final float MIN_EXPENSIVE_UPDATE_DELAY_SEC = 1.0f;
	private static final float ENGINE_IDLE_FRAME_DELAY = 0.32f;
	private static final float ENGINE_ACTIVE_FRAME_DELAY = 0.08f;
	
	////
	
	public class EngineFX
	{
		ShipAPI ship;
		AnimationAPI engine_anim;
		float accumulator = 0.0f;
		
		public EngineFX( ShipAPI ship, AnimationAPI engine_anim )
		{
			this.ship = ship;
			this.engine_anim = engine_anim;
		}

		public void advance( float amount )
		{
			accumulator += amount;
			float frame_delay = ship.getEngineController().isAccelerating() ? ENGINE_ACTIVE_FRAME_DELAY : ENGINE_IDLE_FRAME_DELAY;
			if( accumulator >= frame_delay )
			{
				accumulator -= frame_delay;
				next_frame();
			}
		}
		
		public void next_frame()
		{
			int f = engine_anim.getFrame() + 1;
			if( f >= engine_anim.getNumFrames() )
				f = 0;
			engine_anim.setFrame( f );
		}
	}
	
	////
	
	public void init( CombatEngineAPI engine )
	{
		this.engine = engine;
	}

	public void advance( float amount, List events )
	{
		if( engine.isPaused() )
			return;
		accumulator += amount;
		if( accumulator < MIN_EXPENSIVE_UPDATE_DELAY_SEC )
		{
			do_cheap_update( amount );
		}
		else // accumulator >= MIN_SEARCH_DELAY_SEC
		{
			accumulator -= MIN_EXPENSIVE_UPDATE_DELAY_SEC;
			do_expensive_update();
			do_cheap_update( amount );
		}
	}
	
	public void do_expensive_update()
	{
		tracker.clear();
		for( Iterator s = engine.getShips().iterator(); s.hasNext(); )
		{
			ShipAPI ship = (ShipAPI) s.next();
			if( ship == null || ship.isHulk() )
				continue;
			if( !"nom_roadrunner".equals( ship.getHullSpec().getHullId() ))
				continue;
			WeaponAPI engine_fx = get_weapon_by_slot_name( ship, "engine_fx" );
			if( engine_fx != null )
				tracker.add( new EngineFX( ship, engine_fx.getAnimation() ));
		}
	}
	
	public void do_cheap_update( float amount )
	{
		for( Iterator e = tracker.iterator(); e.hasNext(); )
		{
			EngineFX engine_fx = (EngineFX) e.next();
			engine_fx.advance( amount );
		}
	}
	
	public WeaponAPI get_weapon_by_slot_name( ShipAPI ship, String slot_name )
	{
		for( Iterator w = ship.getAllWeapons().iterator(); w.hasNext(); )
		{
			WeaponAPI weapon = (WeaponAPI) w.next();
			if( slot_name.equals( weapon.getSlot().getId() ))
			{
				return weapon;
			}
		}
		return null;
	}
}
