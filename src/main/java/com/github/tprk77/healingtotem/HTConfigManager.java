package com.github.tprk77.healingtotem;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author tim
 */
public class HTConfigManager {

    private final HTPlugin plugin;

    private final String filename = "config.yml";

    private final int def_totemsperplayer = 100;
    private final boolean def_lightning = true;
    private final boolean def_quiet = false;
    private final int def_stackedheal = 4;
    private final int def_stackeddamage = 4;

    private int totemsperplayer;
    private boolean lightning;
    private boolean quiet;

    private int playerstackedheal;
    private int playerstackeddamage;

    private int mobstackedheal;
    private int mobstackeddamage;

    private int tamedwolfstackedheal;
    private int tamedwolfstackeddamage;

    private int angrywolfstackedheal;
    private int angrywolfstackeddamage;

    public HTConfigManager(HTPlugin plugin){
        this.plugin = plugin;
    }

    public void loadConfigOrDefault(){

        File configfile = new File(plugin.getDataFolder(), filename);
        if(!configfile.isFile()){
            try{
                configfile.getParentFile().mkdirs();
                configfile.createNewFile();
                saveDefaultConfig();
            }catch(Exception ex){
                plugin.getLogger().warning("Could not create file " + configfile.getName());
            }
        }

        loadConfig();
    }

    private void loadConfig(){

        File configfile = new File(plugin.getDataFolder(), filename);
        FileConfiguration conf = new YamlConfiguration();
        try {
            conf.load(configfile);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 

        totemsperplayer = conf.getInt("totemsperplayer", totemsperplayer);
        lightning = conf.getBoolean("lightning", def_lightning);
        quiet = conf.getBoolean("quiet", def_quiet);
                
        if (conf.isInt("player.stackedheal")) {
            playerstackedheal = conf.getInt("player.stackedheal", def_stackedheal);
        } else {
            playerstackedheal = def_stackedheal;
        }
        if (conf.isInt("player.stackeddamage")) {
            playerstackeddamage = conf.getInt("player.stackeddamage", def_stackeddamage);
        } else {
            playerstackeddamage = def_stackeddamage;
        }
        
        if (conf.isInt("mob.stackedheal")) {
            mobstackedheal = conf.getInt("mob.stackedheal", def_stackedheal);
        } else {
            mobstackedheal = def_stackedheal;
        }
        if (conf.isInt("mob.stackeddamage")) {
            mobstackeddamage = conf.getInt("mob.stackeddamage", def_stackeddamage);
        } else {
            mobstackeddamage = def_stackeddamage;
        }
        
        if (conf.isInt("tamedwolf.stackedheal")) {
            tamedwolfstackedheal = conf.getInt("tamedwolf.stackedheal", def_stackedheal);
        } else {
            tamedwolfstackedheal = def_stackedheal;
        }
        if (conf.isInt("tamedwolf.stackeddamage")) {
            tamedwolfstackeddamage = conf.getInt("tamedwolf.stackeddamage", def_stackeddamage);
        } else {
            tamedwolfstackeddamage = def_stackeddamage;
        }
        
        if (conf.isInt("angrywolf.stackedheal")) {
            angrywolfstackedheal = conf.getInt("angrywolf.stackedheal", def_stackedheal);
        } else {
            angrywolfstackedheal = def_stackedheal;
        }
        if (conf.isInt("angrywolf.stackeddamage")) {
            angrywolfstackeddamage = conf.getInt("angrywolf.stackeddamage", def_stackeddamage);
        } else {
            angrywolfstackeddamage = def_stackeddamage;
        }
    }

    private void saveDefaultConfig(){

        File configfile = new File(plugin.getDataFolder(), filename);
        FileConfiguration conf = new YamlConfiguration();
        try {
            conf.load(configfile);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 

        HashMap<String, Object> yamlmap = new HashMap<String, Object>();
        conf.options().copyDefaults(true);

        conf.addDefault("totemsperplayer", def_totemsperplayer);
        conf.addDefault("lightning", def_lightning);
        conf.addDefault("quiet", def_quiet);

        yamlmap = new HashMap<String, Object>();
        yamlmap.put("stackedheal", def_stackedheal);
        yamlmap.put("stackeddamage", def_stackeddamage);
        conf.addDefault("player", yamlmap);

        yamlmap = new HashMap<String, Object>();
        yamlmap.put("stackedheal", def_stackedheal);
        yamlmap.put("stackeddamage", def_stackeddamage);
        conf.addDefault("mob", yamlmap);

        yamlmap = new HashMap<String, Object>();
        yamlmap.put("stackedheal", def_stackedheal);
        yamlmap.put("stackeddamage", def_stackeddamage);
        conf.addDefault("tamedwolf", yamlmap);

        yamlmap = new HashMap<String, Object>();
        yamlmap.put("stackedheal", def_stackedheal);
        yamlmap.put("stackeddamage", def_stackeddamage);
        conf.addDefault("angrywolf", yamlmap);

        try {
            conf.save(configfile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean isLightning(){
        return lightning;
    }

    public boolean isQuiet(){
        return quiet;
    }

    public int getTotemsPerPlayer(){
        return totemsperplayer;
    }

    public int getPlayerStackedHeal(){
        return playerstackedheal;
    }

    public int getMobStackedHeal(){
        return mobstackedheal;
    }

    public int getTamedWolfStackedHeal(){
        return tamedwolfstackedheal;
    }

    public int getAngryWolfStackedHeal(){
        return angrywolfstackedheal;
    }

    public int getPlayerStackedDamage(){
        return playerstackeddamage;
    }

    public int getMobStackedDamage(){
        return mobstackeddamage;
    }

    public int getTamedWolfStackedDamage(){
        return tamedwolfstackeddamage;
    }

    public int getAngryWolfStackedDamage(){
        return angrywolfstackeddamage;
    }
}

