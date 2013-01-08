package com.github.tprk77.healingtotem;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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

    public HTConfigManager(HTPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfigOrDefault() {

        final File configfile = new File(plugin.getDataFolder(), filename);
        if (!configfile.isFile()) {
            try {
                configfile.getParentFile().mkdirs();
                configfile.createNewFile();
                saveDefaultConfig();
            } catch (final Exception ex) {
                plugin.getLogger().warning("Could not create file " + configfile.getName());
            }
        }

        loadConfig();
    }

    private void loadConfig() {

        final File configfile = new File(plugin.getDataFolder(), filename);
        final YamlConfiguration conf = YamlConfiguration.loadConfiguration(configfile);

        totemsperplayer = conf.getInt("totemsperplayer", totemsperplayer);
        lightning = conf.getBoolean("lightning", def_lightning);
        quiet = conf.getBoolean("quiet", def_quiet);

        playerstackedheal = conf.getInt("player.stackedheal", def_stackedheal);
        playerstackeddamage = conf.getInt("player.stackeddamage", def_stackeddamage);

        mobstackedheal = conf.getInt("mob.stackedheal", def_stackedheal);
        mobstackeddamage = conf.getInt("mob.stackeddamage", def_stackeddamage);

        tamedwolfstackedheal = conf.getInt("tamedwolf.stackedheal", def_stackedheal);
        tamedwolfstackeddamage = conf.getInt("tamedwolf.stackeddamage", def_stackeddamage);

        angrywolfstackedheal = conf.getInt("angrywolf.stackedheal", def_stackedheal);
        angrywolfstackeddamage = conf.getInt("angrywolf.stackeddamage", def_stackeddamage);
    }

    private void saveDefaultConfig() {

        final File configfile = new File(plugin.getDataFolder(), filename);
        final YamlConfiguration conf = YamlConfiguration.loadConfiguration(configfile);

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
        } catch (final IOException e) {
            plugin.getLogger().warning("Could not save file " + configfile.getName());
            e.printStackTrace();
        }
    }

    public boolean isLightning() {
        return lightning;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public int getTotemsPerPlayer() {
        return totemsperplayer;
    }

    public int getPlayerStackedHeal() {
        return playerstackedheal;
    }

    public int getMobStackedHeal() {
        return mobstackedheal;
    }

    public int getTamedWolfStackedHeal() {
        return tamedwolfstackedheal;
    }

    public int getAngryWolfStackedHeal() {
        return angrywolfstackedheal;
    }

    public int getPlayerStackedDamage() {
        return playerstackeddamage;
    }

    public int getMobStackedDamage() {
        return mobstackeddamage;
    }

    public int getTamedWolfStackedDamage() {
        return tamedwolfstackeddamage;
    }

    public int getAngryWolfStackedDamage() {
        return angrywolfstackeddamage;
    }
}
