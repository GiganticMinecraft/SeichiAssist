package com.github.unchama.util.external;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
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
            throw new IllegalStateException("WorldGuardPluginが見つかりませんでした。");
        }

        return (WorldGuardPlugin) plugin;
    }

    public static MultiverseCore getMultiverseCore() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");

        if (!(plugin instanceof MultiverseCore)) {
            throw new IllegalStateException("Multiverse-Coreが見つかりませんでした。");
        }

        return (MultiverseCore) plugin;
    }

    //ワールドエディットAPIを返す
    public static WorldEditPlugin getWorldEdit() {
        Plugin pl = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (pl instanceof WorldEditPlugin)
            return (WorldEditPlugin) pl;
        else return null;
    }

    public static TownyAPI getTownyAPI() {
        Plugin pl = Bukkit.getPluginManager().getPlugin("towny");
        if (pl instanceof Towny)
            return TownyAPI.getInstance();
        else return null;
    }
    public static TownyAPIWrapper$ getTownyAPIWrapper() {
        TownyAPI tapi = getTownyAPI();
        return tapi == null ? null : TownyAPIWrapper.instance();
    }
}
