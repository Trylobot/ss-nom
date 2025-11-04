package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEnginePlugin;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.trylobot.TrylobotUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class TheNomadsRetributionWeaponPlugin implements CombatEnginePlugin, EveryFrameCombatPlugin
{
    private static final float RETRIBUTION_LAUNCH_TIMER = 3.0f;
    private static final float RETRIBUTION_ARM_DISTANCE_SQUARED = 35.0f * 35.0f;
    
    private CombatEngineAPI engine = null;
    private HashMap<ShipAPI, Float> fang_hulks = new HashMap<>();
    private HashMap<MissileAPI, ShipAPI> unarmed_retribution_missiles = new HashMap<>();
    private IntervalUtil interval = new IntervalUtil(0.5f, 1.5f);
    private float clock = 0.0f;
    
    @Override
    public void init(CombatEngineAPI engine)
    {
        this.engine = engine;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events)
    {
        if (engine == null || engine.isPaused())
            return;
        
        clock += amount;
        interval.advance(amount);
        if (!interval.intervalElapsed())
            return;
        
        // Find new hulked fangs
        for (Iterator<ShipAPI> i = engine.getShips().iterator(); i.hasNext(); )
        {
            ShipAPI ship = i.next();
            if (fang_hulks.containsKey(ship))
                continue;
            if (ship.isHulk() && "nom_fang".equals(ship.getHullSpec().getHullId()))
            {
                fang_hulks.put(ship, clock);
                ship.setSprite("nomads", "nom_fang_empty");
                WeaponAPI launcher = get_retribution_weapon(ship);
                if (launcher != null) {
                    launcher.getAnimation().setFrame(2);
                    launcher.getAnimation().pause();
                }
            }
        }

        // Check timers on known hulks
        for (Iterator<Entry<ShipAPI, Float>> i = fang_hulks.entrySet().iterator(); i.hasNext(); )
        {
            Entry<ShipAPI, Float> entry = i.next();
            ShipAPI fang_hulk = entry.getKey();
            WeaponAPI launcher = get_retribution_weapon(fang_hulk);
            if (fang_hulk == null || launcher == null || !engine.isEntityInPlay(fang_hulk))
            {
                i.remove();
                continue;
            }
            Float found_clock_time = entry.getValue();
            if (clock >= found_clock_time + RETRIBUTION_LAUNCH_TIMER)
            {
                launcher.getAnimation().setFrame(0);
                launcher.getAnimation().pause();
                MissileAPI missile = (MissileAPI) engine.spawnProjectile(
                    fang_hulk, launcher, "nom_retribution_postmortem_launcher",
                    fang_hulk.getLocation(), fang_hulk.getFacing(), fang_hulk.getVelocity());
                missile.setAngularVelocity(fang_hulk.getAngularVelocity());
                missile.setCollisionClass(CollisionClass.NONE);
                Global.getSoundPlayer().playSound("nom_retribution_launch",
                    1.0f + (0.2f * (float)Math.random() - 0.1f),
                    1.0f + (0.2f * (float)Math.random() - 0.1f),
                    fang_hulk.getLocation(), fang_hulk.getVelocity());
                unarmed_retribution_missiles.put(missile, fang_hulk);
                entry.setValue(Float.MAX_VALUE);
            }
        }

        // Check timers on unarmed retribution missiles
        for (Iterator<Entry<MissileAPI, ShipAPI>> i = unarmed_retribution_missiles.entrySet().iterator(); i.hasNext(); )
        {
            Entry<MissileAPI, ShipAPI> entry = i.next();
            MissileAPI missile = entry.getKey();
            ShipAPI launching_hulk = entry.getValue();
            if (TrylobotUtils.get_distance_squared(missile.getLocation(), launching_hulk.getLocation()) >= RETRIBUTION_ARM_DISTANCE_SQUARED)
            {
                missile.setCollisionClass(CollisionClass.MISSILE_NO_FF);
                i.remove();
            }
        }
    }
    
    private WeaponAPI get_retribution_weapon(ShipAPI fang)
    {
        for (WeaponAPI weapon : fang.getAllWeapons())
        {
            if ("nom_retribution_postmortem_launcher".equals(weapon.getId()))
                return weapon;
        }
        return null;
    }

    @Override
    public void renderInWorldCoords(ViewportAPI vapi) {
        // Optional rendering here
    }

    @Override
    public void renderInUICoords(ViewportAPI vapi) {
        // Optional rendering here
    }

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
        // No input needed
    }
}
