package com.github.tprk77.healingtotem;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author tim
 */
public final class HTPlugin extends JavaPlugin {

    private static HTPlugin instance = null;
    private HTConfigManager configManager;
    private HTTotemManager totemManager;

    @Override
    public void onEnable() {
        instance = this;

        getServer().getPluginManager().registerEvents(new HTListener(this), this);

        configManager = new HTConfigManager(this);
        configManager.loadConfigOrDefault();

        totemManager = new HTTotemManager(this);
        totemManager.loadTotemTypesOrDefault();
        totemManager.loadTotems();
    }

    @Override
    public void onDisable() {
        totemManager.cancelAllTasks();
        instance = null;
    }

    public HTTotemManager getTotemManager() {
        return totemManager;
    }

    public HTConfigManager getConfigManager() {
        return configManager;
    }

    public static HTPlugin getInstance() {
        return instance;
    }
}
