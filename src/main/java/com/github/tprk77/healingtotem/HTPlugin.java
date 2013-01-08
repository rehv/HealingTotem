package com.github.tprk77.healingtotem;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author tim
 */
public final class HTPlugin extends JavaPlugin {

    private HTConfigManager configManager;
    private HTTotemManager totemManager;
    private HTHealerRunnable healerRunnable;

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(new HTListener(this), this);

        configManager = new HTConfigManager(this);
        configManager.loadConfigOrDefault();

        totemManager = new HTTotemManager(this);
        totemManager.loadTotemTypesOrDefault();
        totemManager.loadTotems();

        healerRunnable = new HTHealerRunnable(this);
        healerRunnable.schedule();
    }

    @Override
    public void onDisable() {
        healerRunnable.cancel();
    }

    public HTTotemManager getTotemManager() {
        return totemManager;
    }

    public HTConfigManager getConfigManager() {
        return configManager;
    }
}
