package org.artaphy.betterDoors;

import org.artaphy.betterDoors.listeners.DoorListener;
import org.artaphy.betterDoors.listeners.DoorsListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterDoors extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("BetterDoors plugin has been enabled!");
        getServer().getPluginManager().registerEvents(new DoorsListener(this), this);
        getServer().getPluginManager().registerEvents(new DoorListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("BetterDoors plugin has been disabled!");
    }
}
