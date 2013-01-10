package com.github.tprk77.healingtotem;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;
import org.bukkit.scheduler.BukkitScheduler;

import com.github.tprk77.healingtotem.totemdao.Totem;

/**
 * 
 * @author tim
 */
public class HTHealerRunnable implements Runnable {

    private final HTPlugin plugin;
    private final String totemTypeName;

    private int taskID;
    private List<LivingEntityProcessor> processors;

    HTHealerRunnable(HTPlugin plugin, String totemType) {
        this.plugin = plugin;
        this.totemTypeName = totemType;
        processors = new ArrayList<LivingEntityProcessor>();

        LivingEntityProcessor processor;

        // for players
        final HTConfigManager config = plugin.getConfigManager();
        processor = new LivingEntityProcessor(plugin.getServer().getPluginManager(), config.getPlayerStackedHeal(), config.getPlayerStackedDamage(), config.getPlayerStackedSatiety(), config.getPlayerStackedHunger()) {
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems) {
                if (!(entity instanceof Player))
                    return false;
                final Player player = (Player) entity;
                final boolean canbehealed = player.hasPermission("healingtotem.heal");
                final boolean canbedamaged = player.hasPermission("healingtotem.damage");
                final boolean canbesatiated = player.hasPermission("healingtotem.satiety");
                final boolean canbehungered = player.hasPermission("healingtotem.hunger");
                if (!canbehealed && !canbedamaged && !canbesatiated && !canbehungered)
                    return false;

                int healingpower = sumTotemEffectiveHealingPower(entity, totems);
                if (healingpower > 0 && !canbehealed)
                    healingpower = 0;
                if (healingpower < 0 && !canbedamaged)
                    healingpower = 0;
                if (healingpower != 0)
                    applyHeal(entity, healingpower);

                int foodpower = sumTotemEffectiveFoodPower(entity, totems);
                if (foodpower > 0 && !canbesatiated)
                    foodpower = 0;
                if (foodpower < 0 && !canbehungered)
                    foodpower = 0;
                if (foodpower != 0)
                    applyFood(player, foodpower);

                return true;
            }
        };
        processors.add(processor);

        // for monsters
        processor = new LivingEntityProcessor(plugin.getServer().getPluginManager(), config.getMobStackedHeal(), config.getMobStackedDamage()) {
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems) {
                if (!(entity instanceof Monster))
                    return false;
                final int power = sumTotemEffectiveHealingPower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);

        // ghasts are not technically monsters, but they are now...
        processor = new LivingEntityProcessor(plugin.getServer().getPluginManager(), config.getMobStackedHeal(), config.getMobStackedDamage()) {
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems) {
                if (!(entity instanceof Ghast))
                    return false;
                final int power = sumTotemEffectiveHealingPower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);

        // for huge slimes (and bigger)
        processor = new LivingEntityProcessor(plugin.getServer().getPluginManager(), config.getMobStackedHeal(), config.getMobStackedDamage()) {
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems) {
                if (!(entity instanceof Slime && ((Slime) entity).getSize() >= 8))
                    return false;
                final int power = sumTotemEffectiveHealingPower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);

        // for big slimes
        processor = new LivingEntityProcessor(plugin.getServer().getPluginManager(), config.getMobStackedHeal(), config.getMobStackedDamage()) {
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems) {
                if (!(entity instanceof Slime && ((Slime) entity).getSize() >= 4 && ((Slime) entity).getSize() < 8))
                    return false;
                final int power = sumTotemEffectiveHealingPower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);

        // for small slimes
        processor = new LivingEntityProcessor(plugin.getServer().getPluginManager(), config.getMobStackedHeal(), config.getMobStackedDamage()) {
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems) {
                if (!(entity instanceof Slime && ((Slime) entity).getSize() >= 2 && ((Slime) entity).getSize() < 4))
                    return false;
                final int power = sumTotemEffectiveHealingPower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);

        // for tiny slimes
        processor = new LivingEntityProcessor(plugin.getServer().getPluginManager(), config.getMobStackedHeal(), config.getMobStackedDamage()) {
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems) {
                if (!(entity instanceof Slime && ((Slime) entity).getSize() < 2))
                    return false;
                final int power = sumTotemEffectiveHealingPower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);

        // for tamed wolves
        processor = new LivingEntityProcessor(plugin.getServer().getPluginManager(), config.getTamedWolfStackedHeal(), config.getTamedWolfStackedDamage()) {
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems) {
                if (!(entity instanceof Wolf) || !((Wolf) entity).isTamed())
                    return false;
                final int power = sumTotemEffectiveHealingPower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);

        // for angry wolves
        processor = new LivingEntityProcessor(plugin.getServer().getPluginManager(), config.getAngryWolfStackedHeal(), config.getAngryWolfStackedDamage()) {
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems) {
                if (!(entity instanceof Wolf) || !((Wolf) entity).isAngry())
                    return false;
                final int power = sumTotemEffectiveHealingPower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);
    }

    public void schedule(int updaterate) {
        final BukkitScheduler scheduler = plugin.getServer().getScheduler();
        taskID = scheduler.scheduleSyncRepeatingTask(plugin, this, 0, updaterate);
        if (taskID == -1) {
            plugin.getLogger().warning("Failed to schedule task for TotemType " + totemTypeName + " !");
        }
    }

    public void cancel() {
        final BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.cancelTask(taskID);
    }

    public void run() {

        final List<Totem> totems = new ArrayList<Totem>();
        final List<World> worlds = plugin.getServer().getWorlds();

        for (final Totem totem : plugin.getTotemManager().getTotems()) {
            if (totem.getTotemType().getName().equals(totemTypeName)) {
                totems.add(totem);
            }
        }

        for (final World world : worlds) {
            final List<LivingEntity> livingentities = world.getLivingEntities();
            for (final LivingEntity livingentity : livingentities) {
                for (final LivingEntityProcessor processor : processors) {
                    if (processor.process(livingentity, totems))
                        break;
                }
            }
        }
    }
}
