package com.github.unchama.seichiassist.util.external;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UtilityClassCanBeEnum")
public final class ExternalPlugins {
    private ExternalPlugins() {

    }


    //コアプロテクトAPIを返す
    private static CoreProtectAPI getCoreProtect() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (!(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        final CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (!CoreProtect.isEnabled()) {
            return null;
        }

        // Check that a compatible version of the API is loaded
        if (CoreProtect.APIVersion() < 4) {
            return null;
        }

        return CoreProtect;
    }

    @Nullable
    public static CoreProtectWrapper getCoreProtectWrapper() {
        final CoreProtectAPI cp = getCoreProtect();

        return cp == null ? null : new CoreProtectWrapper(cp);
    }

    //ワールドガードAPIを返す
    public static WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (!(plugin instanceof WorldGuardPlugin)) {
            throw new NullPointerException("WorldGuardPluginが見つかりませんでした。");
        }

        return (WorldGuardPlugin) plugin;
    }

    //ワールドエディットAPIを返す
    public static WorldEditPlugin getWorldEdit() {
        Plugin pl = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (pl instanceof WorldEditPlugin)
            return (WorldEditPlugin) pl;
        else return null;
    }
}
