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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import com.github.tprk77.healingtotem.totemdao.Totem;

/**
 *
 * @author tim
 */
public class HTHealerRunnable implements Runnable {

    private final HTPlugin plugin;

    private final int period = 20;

    private int taskID;
    private List<LivingEntityProcessor> processors;

    private abstract class LivingEntityProcessor {

        private final PluginManager eventcaller;
        private final int stackedheal;
        private final int stackeddamage;
        private final int maxhealth;

        public LivingEntityProcessor(PluginManager eventcaller, int stackedheal, int stackeddamage, int maxhealth){
            this.eventcaller = eventcaller;
            this.stackedheal = stackedheal;
            this.stackeddamage = stackeddamage;
            this.maxhealth = maxhealth;
        }

        public abstract boolean process(LivingEntity entity, List<Totem> totems);

        protected int sumTotemEffectivePower(LivingEntity entity, List<Totem> totems){
            int power = 0;
            for(Totem totem : totems){
                if(totem.inRange(entity)){
                    power += totem.getEffectivePower(entity);
                }
            }
            return power;
        }

        protected void applyHeal(LivingEntity entity, int power){

            /*
             * Only let the events be cancelled. For now don't let the event modify
             * the power. Just use the original power and disregard the event power
             * (if it has been changed).
             *
             * Then again... Maybe I'll change my mind.
             */

            if(power > stackedheal){
                power = stackedheal;
            }else if(power < -stackeddamage){
                power = -stackeddamage;
            }

            int health = entity.getHealth();

            if(power > 0){
                EntityRegainHealthEvent regen = new EntityRegainHealthEvent(
                                entity, power, EntityRegainHealthEvent.RegainReason.CUSTOM);
                eventcaller.callEvent(regen);
                if(regen.isCancelled()){
                    return;
                }
            }else if(power < 0){
                EntityDamageEvent damage = new EntityDamageEvent(
                                entity, EntityDamageEvent.DamageCause.CUSTOM, -power);
                eventcaller.callEvent(damage);
                if(damage.isCancelled()){
                    return;
                }
            }else{
                return;
            }

            int newhealth = health + power;
            if(newhealth > maxhealth){
                newhealth = maxhealth;
            }

            if(newhealth > health){
                entity.setHealth(newhealth);
            }else if(newhealth < health){
                entity.damage(-power);
            }
        }
    }

    HTHealerRunnable(HTPlugin plugin){
        this.plugin = plugin;
        processors = new ArrayList<LivingEntityProcessor>();

        LivingEntityProcessor processor;

        // for players
        processor = new LivingEntityProcessor(
                        plugin.getServer().getPluginManager(),
                        plugin.getConfigManager().getPlayerStackedHeal(),
                        plugin.getConfigManager().getPlayerStackedDamage(),
                        20){
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems){
                if(!(entity instanceof Player)) return false;
                Player player = (Player) entity;
                boolean canbehealed = player.hasPermission("healingtotem.heal");
                boolean canbedamaged = player.hasPermission("healingtotem.damage");
                if(!canbehealed && !canbedamaged) return false;
                int power = sumTotemEffectivePower(entity, totems);
                if(power > 0 && !canbehealed) power = 0;
                if(power < 0 && !canbedamaged) power = 0;
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);

        // for monsters
        processor = new LivingEntityProcessor(
                        plugin.getServer().getPluginManager(),
                        plugin.getConfigManager().getMobStackedHeal(),
                        plugin.getConfigManager().getMobStackedDamage(),
                        20){
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems){
                if(!(entity instanceof Monster)) return false;
                int power = sumTotemEffectivePower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);

        // ghasts are not technically monsters, but they are now...
        processor = new LivingEntityProcessor(
                        plugin.getServer().getPluginManager(),
                        plugin.getConfigManager().getMobStackedHeal(),
                        plugin.getConfigManager().getMobStackedDamage(),
                        10){
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems){
                if(!(entity instanceof Ghast)) return false;
                int power = sumTotemEffectivePower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);

        // for huge slimes (and bigger)
        processor = new LivingEntityProcessor(
                        plugin.getServer().getPluginManager(),
                        plugin.getConfigManager().getMobStackedHeal(),
                        plugin.getConfigManager().getMobStackedDamage(),
                        32){
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems){
                if(!(entity instanceof Slime && ((Slime)entity).getSize() >= 8)) return false;
                int power = sumTotemEffectivePower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);

        // for big slimes
        processor = new LivingEntityProcessor(
                        plugin.getServer().getPluginManager(),
                        plugin.getConfigManager().getMobStackedHeal(),
                        plugin.getConfigManager().getMobStackedDamage(),
                        16){
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems){
                if(!(entity instanceof Slime && ((Slime)entity).getSize() >= 4 && ((Slime)entity).getSize() < 8)) return false;
                int power = sumTotemEffectivePower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);

        // for small slimes
        processor = new LivingEntityProcessor(
                        plugin.getServer().getPluginManager(),
                        plugin.getConfigManager().getMobStackedHeal(),
                        plugin.getConfigManager().getMobStackedDamage(),
                        4){
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems){
                if(!(entity instanceof Slime && ((Slime)entity).getSize() >= 2 && ((Slime)entity).getSize() < 4)) return false;
                int power = sumTotemEffectivePower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);

        // for tiny slimes
        processor = new LivingEntityProcessor(
                        plugin.getServer().getPluginManager(),
                        plugin.getConfigManager().getMobStackedHeal(),
                        plugin.getConfigManager().getMobStackedDamage(),
                        1){
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems){
                if(!(entity instanceof Slime && ((Slime)entity).getSize() < 2)) return false;
                int power = sumTotemEffectivePower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);

        // for tamed wolves
        processor = new LivingEntityProcessor(
                        plugin.getServer().getPluginManager(),
                        plugin.getConfigManager().getTamedWolfStackedHeal(),
                        plugin.getConfigManager().getTamedWolfStackedDamage(),
                        20){
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems){
                if(!(entity instanceof Wolf) || !((Wolf) entity).isTamed()) return false;
                int power = sumTotemEffectivePower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);

        // for angry wolves
        processor = new LivingEntityProcessor(
                        plugin.getServer().getPluginManager(),
                        plugin.getConfigManager().getAngryWolfStackedHeal(),
                        plugin.getConfigManager().getAngryWolfStackedDamage(),
                        8){
            @Override
            public boolean process(LivingEntity entity, List<Totem> totems){
                if(!(entity instanceof Wolf) || !((Wolf) entity).isAngry()) return false;
                int power = sumTotemEffectivePower(entity, totems);
                applyHeal(entity, power);
                return true;
            }
        };
        processors.add(processor);
    }

    public void schedule(){
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        taskID = scheduler.scheduleSyncRepeatingTask(plugin, this, 0, period);
        if(taskID == -1){
            plugin.getLogger().warning("Failed to schedule!");
        }
    }

    public void cancel(){
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.cancelTask(taskID);
    }

    public void run(){

        List<Totem> totems = plugin.getTotemManager().getTotems();
        List<World> worlds = plugin.getServer().getWorlds();

        for(World world : worlds){
            List<LivingEntity> livingentities = world.getLivingEntities();
            for(LivingEntity livingentity : livingentities){
                for(LivingEntityProcessor processor : processors){
                    if(processor.process(livingentity, totems)) break;
                }
            }
        }
    }
}
