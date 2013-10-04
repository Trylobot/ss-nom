package data.hullmods;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.hullmods.base.BaseFleetEffectHullMod;
import data.scripts._;


public class TheNomadsAutomatedNanobotFabricators extends BaseFleetEffectHullMod
{
	private boolean enabled = false;
	private float accumulator = 0.0f;
	
	private final float EXECUTION_PERIOD_DAYS = 1.0f; // how often in days to generate supplies
	private final float SUPPLIES_ADD = 50.0f; // number of supplies to add each generation step
	private final float CARGO_CAPACITY_MAX = 0.5f; // if cargo is more than 50% full, do not generate new supplies
	
	public void applyEffectsBeforeShipCreation( HullSize hullSize, MutableShipStatsAPI stats, String id )
	{
		if( hullSize != HullSize.CAPITAL_SHIP )
			return;

		enabled = true;
	}
	
	public void advanceInCampaign( FleetMemberAPI member, float amount )
	{
		if( !enabled )
			return;
		
		accumulator += amount;
		if( accumulator >= EXECUTION_PERIOD_DAYS )
			accumulator -= EXECUTION_PERIOD_DAYS;
		else
			return;
		
		CampaignFleetAPI fleet = findFleet( member );
		if( fleet == null )
			return; // this will only happen if there are fleets that I've not searched
		
		CargoAPI cargo = fleet.getCargo();
		float max_cargo = CARGO_CAPACITY_MAX * cargo.getMaxCapacity();
		
		if( cargo.getSpaceUsed() <= max_cargo )
		{
			fleet.getCargo().addSupplies( SUPPLIES_ADD );
			//_.L("Nanobot Factories created "+SUPPLIES_ADD+" supplies");
		}
	}
}
