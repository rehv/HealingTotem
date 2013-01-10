package com.github.tprk77.healingtotem;

import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.PluginManager;

import com.github.tprk77.healingtotem.totemdao.Totem;

public abstract class LivingEntityProcessor {

    private final PluginManager eventcaller;
    private final int stackedheal;
    private final int stackeddamage;

    public LivingEntityProcessor(PluginManager eventcaller, int stackedheal, int stackeddamage) {
        this.eventcaller = eventcaller;
        this.stackedheal = stackedheal;
        this.stackeddamage = stackeddamage;
    }

    public abstract boolean process(LivingEntity entity, List<Totem> totems);

    protected int sumTotemEffectivePower(LivingEntity entity, List<Totem> totems) {
        int power = 0;
        for (final Totem totem : totems) {
            if (totem.inRange(entity)) {
                power += totem.getEffectivePower(entity);
            }
        }
        return power;
    }

    protected void applyHeal(LivingEntity entity, int power) {

        /*
         * Only let the events be cancelled. For now don't let the event modify
         * the power. Just use the original power and disregard the event power
         * (if it has been changed).
         *
         * Then again... Maybe I'll change my mind.
         */

        if (power > stackedheal) {
            power = stackedheal;
        } else if (power < -stackeddamage) {
            power = -stackeddamage;
        }

        final int health = entity.getHealth();

        if (power > 0) {
            final EntityRegainHealthEvent regen = new EntityRegainHealthEvent(entity, power, EntityRegainHealthEvent.RegainReason.CUSTOM);
            eventcaller.callEvent(regen);
            if (regen.isCancelled()) {
                return;
            }
        } else if (power < 0) {
            final EntityDamageEvent damage = new EntityDamageEvent(entity, EntityDamageEvent.DamageCause.CUSTOM, -power);
            eventcaller.callEvent(damage);
            if (damage.isCancelled()) {
                return;
            }
        } else {
            return;
        }

        int newhealth = health + power;
        if (newhealth > entity.getMaxHealth()) {
            newhealth = entity.getMaxHealth();
        }

        if (newhealth > health) {
            entity.setHealth(newhealth);
        } else if (newhealth < health) {
            entity.damage(-power);
        }
    }
}
