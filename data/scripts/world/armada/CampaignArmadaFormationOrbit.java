package data.scripts.world.armada;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import data.scripts.math.FastTrig;
import data.scripts.world.armada.api.CampaignArmadaAPI;
import data.scripts.world.armada.api.CampaignArmadaEscortFleetPositionerAPI;
import org.lwjgl.util.vector.Vector2f;

public class CampaignArmadaFormationOrbit implements CampaignArmadaEscortFleetPositionerAPI
{
	//private final static float TWO_PI = (float)(2.0f*Math.PI);

	private SectorAPI sector;
	private float orbit_radius;
	private float orbit_direction;
	private float orbit_period_days;
	private float seconds_per_day;
	
	//private CampaignClockAPI clock;
	private float accumulator;
	
	public CampaignArmadaFormationOrbit(
	  SectorAPI sector,
	  float orbit_radius,
	  float orbit_direction, // positive is counter-clockwise
	  float orbit_period_days )
	{
		this.sector = sector;
		this.orbit_radius = orbit_radius;
		this.orbit_direction = Math.signum( orbit_direction );
		this.orbit_period_days = orbit_period_days;
		
		//this.clock = sector.getClock();
		this.seconds_per_day = sector.getClock().getSecondsPerDay();
	}
	
	/*// persistent allocations
	private CampaignFleetAPI _escF;
	private float _r; // radius
	private Vector2f _LP; // leader position
	private Vector2f _LV; // leader velocity
	private float _phA; // phase angle constant
	private float _eA; // escort fleet angle
	//private Vector2f _eP = new Vector2f(); // escort fleet position
	private float _TS_FACTOR = 100000000.0f; // arbitrary (do not modify)
	private float _o_sK = 6.0f; // orbit speed factor (could be parameterized)
	private float _o_s = _TS_FACTOR / _o_sK; // orbit speed*/
	////
	
	@Override
	public void update_escort_fleet_positions( float amount, CampaignArmadaAPI armada )
	{
		accumulator += amount;
		CampaignFleetAPI leader_fleet = armada.getLeaderFleet();
		CampaignFleetAPI[] escort_fleets = armada.getEscortFleets();
		Vector2f leader_fleet_location = leader_fleet.getLocation();
		for( int i = 0; i < escort_fleets.length; ++i )
		{
			CampaignFleetAPI escort_fleet = escort_fleets[i];
			if( escort_fleet == null || !escort_fleet.isAlive() || escort_fleet.isInHyperspaceTransition() )
				continue;
			float formation_angle_offset = 360.0f / escort_fleets.length;
			float orbit_angle = (accumulator / seconds_per_day) * orbit_period_days * 360.0f + formation_angle_offset;
			escort_fleet.setLocation(
			    (float)(leader_fleet_location.x + orbit_radius * FastTrig.cos( orbit_angle )),
			    (float)(leader_fleet_location.y + orbit_radius * FastTrig.sin( orbit_angle )) );
		}
		
		/*// get position, speed, and direction of leader fleet.
		// leader fleet assumed to 
		_LP = leader_fleet.getLocation();
		_LV = leader_fleet.getVelocity();

		// separation distance is the radius of the orbit.
		// escort fleets are evenly-spaced around the circumference of the orbit.
		// the orbit phase is locked to allow for slow fleets
		_phA = (float)((clock.getTimestamp()%((long)(TWO_PI*_o_s)))/_o_s);

		_r = orbit_radius;

		for( int i = 0; i < escort_fleets.length; ++i )
		{
			_escF = escort_fleets[i];
			if( _escF.isAlive() && !_escF.isInHyperspaceTransition() ) // do not move dead or jumping fleets ever
			{
				_eA = orbit_direction * i*(TWO_PI/(float)escort_fleets.length) + _phA;
				_escF.setLocation(
				  (float)(_LP.x + _r*Math.cos( _eA )),
				  (float)(_LP.y + _r*Math.sin( _eA )) );
				_escF.getVelocity().set( _LV ); // for proper hyperspace fuel burn
			}
		}*/
	}
}
