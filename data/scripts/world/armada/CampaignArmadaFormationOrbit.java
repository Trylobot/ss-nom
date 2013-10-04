/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.world.armada;

import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import data.scripts.world.armada.api.CampaignArmadaEscortFleetPositionerAPI;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author tbot
 */
public class CampaignArmadaFormationOrbit implements CampaignArmadaEscortFleetPositionerAPI
{
	private final static float TWO_PI = (float)(2.0f*Math.PI);

	private SectorAPI sector;
	private float orbitRadius;
	private float orbitDirection;
	private float orbitPeriodDays;
	private float seconds_per_day;
	
	private CampaignClockAPI clock;
	
	
	public CampaignArmadaFormationOrbit(
	  SectorAPI sector,
	  float orbitRadius,
	  float orbitDirection, // positive is counter-clockwise
	  float orbitPeriodDays )
	{
		this.sector = sector;
		this.orbitRadius = orbitRadius;
		this.orbitDirection = Math.signum( orbitDirection );
		this.orbitPeriodDays = orbitPeriodDays;
		
		this.clock = sector.getClock();
		this.seconds_per_day = this.clock.getSecondsPerDay();
	}
	
	// persistent allocations
	private CampaignFleetAPI _escF;
	private float _r; // radius
	private Vector2f _LP; // leader position
	private Vector2f _LV; // leader velocity
	private float _phA; // phase angle constant
	private float _eA; // escort fleet angle
	//private Vector2f _eP = new Vector2f(); // escort fleet position
	private float _TS_FACTOR = 100000000.0f; // arbitrary (do not modify)
	private float _o_sK = 6.0f; // orbit speed factor (could be parameterized)
	private float _o_s = _TS_FACTOR / _o_sK; // orbit speed
	////

	public void update_escort_fleet_positions( float amount, CampaignFleetAPI leader_fleet, CampaignFleetAPI[] escort_fleets )
	{
		// get position, speed, and direction of leader fleet.
		// leader fleet assumed to 
		_LP = leader_fleet.getLocation();
		_LV = leader_fleet.getVelocity();

		// separation distance is the radius of the orbit.
		// escort fleets are evenly-spaced around the circumference of the orbit.
		// the orbit phase is locked to allow for slow fleets
		_phA = (float)((clock.getTimestamp()%((long)(TWO_PI*_o_s)))/_o_s);

		_r = orbitRadius;

		for( int i = 0; i < escort_fleets.length; ++i )
		{
			_escF = escort_fleets[i];
			if( _escF.isAlive() && !_escF.isInHyperspaceTransition() ) // do not move dead or jumping fleets ever
			{
				_eA = orbitDirection * i*(TWO_PI/(float)escort_fleets.length) + _phA;
				_escF.setLocation(
				  (float)(_LP.x + _r*Math.cos( _eA )),
				  (float)(_LP.y + _r*Math.sin( _eA )) );
				_escF.getVelocity().set( _LV ); // for proper hyperspace fuel burn
			}
		}
	}
}
