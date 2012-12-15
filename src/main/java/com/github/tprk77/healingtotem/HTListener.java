package com.github.tprk77.healingtotem;


import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.github.tprk77.healingtotem.totemdao.Totem;
import com.github.tprk77.healingtotem.totemdao.TotemType;

/**
 *
 * @author tim
 */
public class HTListener implements Listener {

    private final HTPlugin plugin;

    //enum SubstructurePolicy {ALLOWED, REPLACE, NOT_ALLOWED};
    //private final SubstructurePolicy substructurepolicy;

    public HTListener(HTPlugin plugin){
        this.plugin = plugin;
        //this.substructurepolicy = SubstructurePolicy.NOT_ALLOWED;
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(event.isCancelled()) return;

        String owner = event.getPlayer().getName();

        Block placedblock = event.getBlockPlaced();
        List<TotemType> totemtypes = plugin.getTotemManager().getTotemTypes();

        totembuild:
        for(TotemType totemtype : totemtypes){

            Totem totem = new Totem(totemtype, placedblock, owner);
            if(!totem.verifyStructure()) {
                continue totembuild;
            }

            // check permissions!
            Player player = event.getPlayer();
            if(!player.hasPermission("healingtotem.build")){
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You do not have permission to build totems.");
                return;
            }

            // check the number of totems
            Set<Totem> totemset = plugin.getTotemManager().getTotemsFromPlayer(player);
            if(totemset != null && totemset.size() >= plugin.getConfigManager().getTotemsPerPlayer() && !player.hasPermission("healingtotem.unlimitedbuild")){
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You have reached the maximum number of totems you can build.");
                return;
            }

            /*
             * Actually implementing REPLACE the right way is going to be really
             * hard. It will require ranking totems by size, and probably always
             * giving priority to the biggest one. But since the shapes are random,
             * probably no policy will work 100% as expected by the user.
             *
             * For example lets say we have T1:
             *
             *  8888
             *  8  8
             *  8888
             *
             * But then we also have T2:
             *
             *  888
             *    8
             *
             * And then we make:
             *
             *  8888
             *  8  888
             *  8888 8
             *
             * It's getting a little to complex. KISS.
             */


//          if(this.substructurepolicy == SubstructurePolicy.NOT_ALLOWED){
            for(Block block : totem.getBlocks()){
                if(plugin.getTotemManager().getTotemsFromBlock(block) != null){
                    break totembuild;
                }
            }
//          }else if(this.substructurepolicy == SubstructurePolicy.REPLACE){
//              // TODO this REPLACE code doesn't work / isn't finished
//              for(Block block : totem.getBlocks()){
//                  Set<Totem> subtotems = this.plugin.getTotemManager().getTotemsFromBlock(block);
//                  if(subtotems != null){
//                      for(Totem subtotem : subtotems){
//                          this.plugin.getTotemManager().removeTotem(subtotem);
//                      }
//                  }
//              }
//          }

            // lightning strike!
            if(plugin.getConfigManager().isLightning()){
                placedblock.getWorld().strikeLightningEffect(placedblock.getLocation());
            }

            plugin.getTotemManager().addTotem(totem);
            plugin.getTotemManager().saveTotems();

            if(!plugin.getConfigManager().isQuiet()){
                player.sendMessage(ChatColor.DARK_AQUA + "A totem has been built.");
            }

            break totembuild;
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(event.isCancelled()) return;

        Block brokenblock = event.getBlock();
        Set<Totem> totems = plugin.getTotemManager().getTotemsFromBlock(brokenblock);

        if(totems == null) return;

        // check permissions!
        Player player = event.getPlayer();
        if(!player.hasPermission("healingtotem.break")){
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You do not have permission to break totems.");
            return;
        }

        // lightning strike!
        if(plugin.getConfigManager().isLightning()){
            brokenblock.getWorld().strikeLightningEffect(brokenblock.getLocation());
        }

        for(Totem totem : totems){
            // TODO add REPLACE code?
            plugin.getTotemManager().removeTotem(totem);
            plugin.getTotemManager().saveTotems();
        }

        if(!plugin.getConfigManager().isQuiet()){
            player.sendMessage(ChatColor.DARK_AQUA + "A totem has been destroyed.");
        }
    }
}

