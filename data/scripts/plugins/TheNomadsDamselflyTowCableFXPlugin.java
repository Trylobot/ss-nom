package data.scripts.plugins;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEnginePlugin;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.trylobot._;
import java.util.Iterator;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;

public class TheNomadsDamselflyTowCableFXPlugin implements CombatEnginePlugin, EveryFrameCombatPlugin
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
		
		for( Iterator i = engine.getShips().iterator(); i.hasNext(); )
		{
			ShipAPI drone = (ShipAPI) i.next();
			if( drone == null || drone.isHulk() )
				continue;
			if( !drone.isDrone() || !"nom_damselfly".equals( drone.getHullSpec().getHullId() ))
				continue; // don't care
			ShipAPI ship = drone.getDroneSource();
			if( ship == null || ship.isHulk() )
				continue;
			WeaponAPI tow_cable = get_tow_cable( drone );
			float angle = get_angle_to_tow_anchor( drone.getLocation(), ship );
			tow_cable.setCurrAngle( angle );
		}
	}
	
	public WeaponAPI get_tow_cable( ShipAPI damselfly_drone )
	{
		return (WeaponAPI) damselfly_drone.getAllWeapons().get( 0 );
	}
	
	public float get_angle_to_tow_anchor( Vector2f tow_cable_source, ShipAPI ship_attached_to )
	{
		// decorative weapon id: nom_tow_cable_anchor
		// weapon slot id: TOW_CABLE_ANCHOR_RIGHT
		// weapon slot id: TOW_CABLE_ANCHOR_LEFT
		WeaponAPI closest_anchor = null;
		float closest_anchor_distance_squared = Float.MAX_VALUE;
		for( Iterator i = ship_attached_to.getAllWeapons().iterator(); i.hasNext(); )
		{
			WeaponAPI weapon = (WeaponAPI) i.next();
			if( "nom_tow_cable_anchor".equals( weapon.getId() ))
			{
				float distance_squared = _.get_distance_squared( tow_cable_source, weapon.getLocation() );
				if( distance_squared < closest_anchor_distance_squared )
				{
					closest_anchor = weapon;
					closest_anchor_distance_squared = distance_squared;
				}
			}
		}
		if( closest_anchor != null )
			return _.get_angle( tow_cable_source, closest_anchor.getLocation() );
		return _.get_angle( tow_cable_source, ship_attached_to.getLocation() );
	}
}
