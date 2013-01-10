package com.github.tprk77.healingtotem;

import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.plugin.PluginManager;

import com.github.tprk77.healingtotem.totemdao.Totem;

public abstract class LivingEntityProcessor {

    private final PluginManager eventcaller;
    private final int stackedheal;
    private final int stackeddamage;
    private final int stackedsatiety;
    private final int stackedhunger;

    public LivingEntityProcessor(PluginManager eventcaller, int stackedheal, int stackeddamage) {
        this(eventcaller, stackedheal, stackeddamage, 0, 0);
    }

    public LivingEntityProcessor(PluginManager eventcaller, int stackedheal, int stackeddamage, int stackedsatiety, int stackedhunger) {
        this.eventcaller = eventcaller;
        this.stackedheal = stackedheal;
        this.stackeddamage = stackeddamage;
        this.stackedsatiety = stackedsatiety;
        this.stackedhunger = stackedhunger;
    }

    public abstract boolean process(LivingEntity entity, List<Totem> totems);

    protected int sumTotemEffectiveFoodPower(LivingEntity entity, List<Totem> totems) {
        int power = 0;
        for (final Totem totem : totems) {
            if (totem.inRange(entity)) {
                power += totem.getEffectiveFoodPower(entity);
            }
        }
        return power;
    }

    protected int sumTotemEffectiveHealingPower(LivingEntity entity, List<Totem> totems) {
        int power = 0;
        for (final Totem totem : totems) {
            if (totem.inRange(entity)) {
                power += totem.getEffectiveHealingPower(entity);
            }
        }
        return power;
    }

    protected void applyFood(Player player, int power) {
        if (power > stackedsatiety) {
            power = stackedsatiety;
        } else if (power < -stackedhunger) {
            power = -stackedhunger;
        }

        int foodlevel = player.getFoodLevel();

        if (foodlevel + power > 20) {
            foodlevel = 20;
        } else if (foodlevel + power < 0) {
            foodlevel = 0;
        } else {
            foodlevel += power;
        }

        final FoodLevelChangeEvent foodEvent = new FoodLevelChangeEvent(player, foodlevel);
        eventcaller.callEvent(foodEvent);
        if (foodEvent.isCancelled()) {
            return;
        }

        player.setFoodLevel(foodlevel);
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
