package org.artaphy.betterDoors;

import org.artaphy.betterDoors.listeners.DoorListener;
import org.artaphy.betterDoors.listeners.DoorsListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * BetterDoors插件主类，继承自JavaPlugin
 */
public final class BetterDoors extends JavaPlugin {

    /**
     * 插件启用时调用的方法
     * 在这里注册默认配置并初始化事件监听器
     */
    @Override
    public void onEnable() {
        saveDefaultConfig(); // 保存默认配置
        getLogger().info("BetterDoors plugin has been enabled!");
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new DoorsListener(this), this);
        getServer().getPluginManager().registerEvents(new DoorListener(this), this);
    }

    /**
     * 插件禁用时调用的方法
     * 在这里执行插件关闭时的清理操作
     */
    @Override
    public void onDisable() {
        getLogger().info("BetterDoors plugin has been disabled!");
        // 这里可以添加插件关闭时的清理操作
    }
}