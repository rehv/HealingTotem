package com.github.tprk77.healingtotem;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * 
 * @author tim
 */
public class HTConfigManager {

    private final HTPlugin plugin;

    private final String filename = "config.yml";

    private final int def_totemsperplayer = 100;
    private final int def_updaterate = 20;
    private final boolean def_lightning = true;
    private final boolean def_quiet = false;
    private final int def_stackedheal = 4;
    private final int def_stackeddamage = 4;
    private final int def_stackedsatiety = 4;
    private final int def_stackedhunger = 4;

    private int totemsperplayer;
    private int updaterate;
    private boolean lightning;
    private boolean quiet;

    private int playerstackedheal;
    private int playerstackeddamage;
    private int playerstackedsatiety;
    private int playerstackedhunger;

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

        totemsperplayer = conf.getInt("totemsperplayer", def_totemsperplayer);
        updaterate = conf.getInt("updaterate", def_updaterate);
        lightning = conf.getBoolean("lightning", def_lightning);
        quiet = conf.getBoolean("quiet", def_quiet);

        playerstackedheal = conf.getInt("player.stackedheal", def_stackedheal);
        playerstackeddamage = conf.getInt("player.stackeddamage", def_stackeddamage);
        playerstackedsatiety = conf.getInt("player.stackedsatiety", def_stackedsatiety);
        playerstackedhunger = conf.getInt("player.stackedhunger", def_stackedhunger);

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

        conf.options().copyDefaults(true);

        conf.addDefault("totemsperplayer", def_totemsperplayer);
        conf.addDefault("updaterate", def_updaterate);
        conf.addDefault("lightning", def_lightning);
        conf.addDefault("quiet", def_quiet);

        conf.addDefault("player.stackedheal", def_stackedheal);
        conf.addDefault("player.stackeddamage", def_stackeddamage);
        conf.addDefault("player.stackedsatiety", def_stackedsatiety);
        conf.addDefault("player.stackedhunger", def_stackedhunger);

        conf.addDefault("mob.stackedheal", def_stackedheal);
        conf.addDefault("mob.stackeddamage", def_stackeddamage);

        conf.addDefault("tamedwolf.stackedheal", def_stackedheal);
        conf.addDefault("tamedwolf.stackeddamage", def_stackeddamage);

        conf.addDefault("angrywolf.stackedheal", def_stackedheal);
        conf.addDefault("angrywolf.stackeddamage", def_stackeddamage);

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

    public int getDefaultUpdateRate() {
        return updaterate;
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

    public int getPlayerStackedSatiety() {
        return playerstackedsatiety;
    }

    public int getPlayerStackedHunger() {
        return playerstackedhunger;
    }
}
