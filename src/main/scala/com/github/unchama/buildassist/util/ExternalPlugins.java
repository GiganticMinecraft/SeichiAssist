package com.github.unchama.buildassist.util;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class ExternalPlugins {
    private ExternalPlugins() {

    }

    //ワールドガードAPIを返す
    public static WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (!(plugin instanceof WorldGuardPlugin)) {
            throw new NullPointerException("WorldGuardPluginが見つかりませんでした。");
        }

        return (WorldGuardPlugin) plugin;
    }

    //ワールドエディットAPIを返す
    public static WorldEditPlugin getWorldEdit() {
        Plugin pl = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if (pl instanceof WorldEditPlugin)
            return (WorldEditPlugin) pl;
        else return null;
    }
}