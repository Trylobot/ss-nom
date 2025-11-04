package data.scripts.plugins;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEnginePlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.trylobot.TrylobotUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class TheNomadsDamselflyTowCableFXPlugin implements CombatEnginePlugin, EveryFrameCombatPlugin {
    private CombatEngineAPI engine;
    private HashMap<WeaponAPI, WeaponAPI> tow_cable_to_anchor_map = new HashMap<>();
    private float accumulator = 0.0f;
    private static final float MIN_SEARCH_DELAY_SEC = 1.0f;
    private static final float MIN_SQUARED_DISTANCE_TO_SHOW_CABLE = 50.0f * 50.0f;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null || engine.isPaused()) return;
        accumulator += amount;
        if (accumulator < MIN_SEARCH_DELAY_SEC) {
            do_cheap_update();
        } else {
            accumulator -= MIN_SEARCH_DELAY_SEC;
            do_expensive_update();
            do_cheap_update();
        }
    }

    public void do_expensive_update() {
        tow_cable_to_anchor_map.clear();
        for (Iterator<ShipAPI> s = engine.getShips().iterator(); s.hasNext(); ) {
            ShipAPI ship = s.next();
            if (ship == null || ship.isHulk())
                continue;
            ShipSystemAPI system = ship.getSystem();
            if (system == null || !"nom_damselfly_drone".equals(system.getId()))
                continue;
            // ship is alive and capable of launching damselflies
            int N = 0;
            for (Iterator<ShipAPI> d = ship.getDeployedDrones().iterator(); d.hasNext(); ) {
                ShipAPI drone = d.next();
                if (!"nom_damselfly".equals(drone.getHullSpec().getHullId()))
                    continue;
                WeaponAPI tow_cable = get_tow_cable(drone);
                if (tow_cable == null)
                    continue;
                WeaponAPI tow_anchor = get_weapon_by_slot_name(ship, "tow_anchor_" + N);
                if (tow_anchor == null)
                    continue;
                ++N;
                tow_cable_to_anchor_map.put(tow_cable, tow_anchor);
            }
        }
    }

    public void do_cheap_update() {
        for (Iterator<Entry<WeaponAPI, WeaponAPI>> t = tow_cable_to_anchor_map.entrySet().iterator(); t.hasNext(); ) {
            Entry<WeaponAPI, WeaponAPI> entry = t.next();
            WeaponAPI tow_cable = entry.getKey();
            WeaponAPI tow_anchor = entry.getValue();
            AnimationAPI anim = tow_cable.getAnimation();
            if (tow_cable.getShip().isHulk()) {
                // drone death
                t.remove();
                anim.setFrame(0); // hide cable
                anim.pause();
                continue;
            }
            // drone alive
            float distance_squared = TrylobotUtils.get_distance_squared(tow_cable.getLocation(), tow_anchor.getLocation());
            if (distance_squared >= MIN_SQUARED_DISTANCE_TO_SHOW_CABLE) {
                float angle = TrylobotUtils.get_angle(tow_cable.getLocation(), tow_anchor.getLocation());
                tow_cable.setCurrAngle(angle);
                anim.setFrame(1 + (((int) (Math.abs(angle))) % 4)); // show cable using frame that changes as it rotates
                anim.pause();
            } else {
                anim.setFrame(0); // hide cable
                anim.pause();
            }
        }
    }

    public WeaponAPI get_tow_cable(ShipAPI damselfly_drone) {
        for (Iterator<WeaponAPI> w = damselfly_drone.getAllWeapons().iterator(); w.hasNext(); ) {
            WeaponAPI weapon = w.next();
            if (!"nom_damselfly_tow_cable".equals(weapon.getId()))
                continue;
            return weapon;
        }
        return null;
    }

    public WeaponAPI get_weapon_by_slot_name(ShipAPI ship, String slot_name) {
        for (Iterator<WeaponAPI> w = ship.getAllWeapons().iterator(); w.hasNext(); ) {
            WeaponAPI weapon = w.next();
            if (slot_name.equals(weapon.getSlot().getId())) {
                return weapon;
            }
        }
        return null;
    }

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
        // No input handling for now
    }

    @Override
    public void renderInWorldCoords(ViewportAPI vapi) {
        // Optional: rendering logic here or leave empty
    }

    @Override
    public void renderInUICoords(ViewportAPI vapi) {
        // Optional: rendering logic here or leave empty
    }
}
