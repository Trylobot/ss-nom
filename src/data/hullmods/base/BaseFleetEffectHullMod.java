package data.hullmods.base;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.combat.BaseHullMod;

import java.util.HashMap;
import java.util.Map;

/**
 * A safer version of the original BaseFleetEffectHullMod that avoids writing
 * game objects into persistent save data (which causes serialization errors).
 *
 * Uses a runtime-only cache (cleared automatically between games).
 */
public abstract class BaseFleetEffectHullMod extends BaseHullMod {

    // Runtime-only cache, shared across all instances (not saved)
    private static final Map<FleetMemberAPI, CampaignFleetAPI> memo = new HashMap<>();

    /**
     * Attempts to find the CampaignFleetAPI associated with the given fleet member.
     * Uses a simple runtime memoization system to speed up repeated lookups.
     */
    public CampaignFleetAPI findFleet(FleetMemberAPI member) {
        if (member == null) return null;

        PersonAPI commander = member.getFleetCommander();
        if (commander == null) return null;

        // Reuse cached fleet if the commander still matches
        CampaignFleetAPI cachedFleet = memo.get(member);
        if (cachedFleet != null && cachedFleet.getCommander() == commander) {
            return cachedFleet;
        }

        // Otherwise, search all star systems for the fleet
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            for (CampaignFleetAPI fleet : system.getFleets()) {
                if (fleet.getCommander() == commander) {
                    memo.put(member, fleet);
                    return fleet;
                }
            }
        }

        // Fleet not found — clear outdated cache entry
        memo.remove(member);
        return null;
    }

    /**
     * Clears the runtime cache manually.
     */
    public static void clearMemo() {
        memo.clear();
    }

    /**
     * Embedded mod plugin to auto-clear cache when a new game starts or returns to title.
     * This ensures that old references never persist between campaigns.
     */
    public static class CacheCleanupPlugin extends BaseModPlugin {
        public void onGameLoad(boolean newGame) {
            clearMemo();
        }
        public void onApplicationLoad() {
            clearMemo();
        }

        public void onNewGameAfterEconomyLoad() {
            clearMemo();
        }

        public void onGameQuit() {
            clearMemo();
        }
    }
}
