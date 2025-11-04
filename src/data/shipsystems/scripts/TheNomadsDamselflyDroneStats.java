package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class TheNomadsDamselflyDroneStats implements ShipSystemStatsScript {

    public static final float ENGINE_TOPSPEED_PERCENT = 10f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        float engineTopSpeedPercent = ENGINE_TOPSPEED_PERCENT * effectLevel;
        stats.getMaxSpeed().modifyPercent(id, engineTopSpeedPercent);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float engineTopSpeedPercent = ENGINE_TOPSPEED_PERCENT * effectLevel;
        if (index == 0) {
            return new StatusData("max speed +" + (int) engineTopSpeedPercent + "%", false);
        }
        return null;
    }

    @Override
    public String getDisplayNameOverride(State state, float effectLevel) {
        return null; // No custom display name
    }

    @Override
    public float getRegenOverride(ShipAPI ship) {
        return -1f; // No regen override
    }

    @Override
    public float getInOverride(ShipAPI ship) {
        return -1f; // No in override
    }

    @Override
    public float getOutOverride(ShipAPI ship) {
        return -1f; // No out override
    }

    @Override
    public float getActiveOverride(ShipAPI ship) {
        return -1f; // No active override
    }

    @Override
    public int getUsesOverride(ShipAPI ship) {
        return -1; // No uses override
    }
}
