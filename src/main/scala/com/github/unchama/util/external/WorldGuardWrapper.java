package com.github.unchama.util.external;

import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * WorldGuardの各種関数を集めたクラスです.
 *
 * @author karayuu
 */
public class WorldGuardWrapper {
    /**
     * ワールドガードのインスタンス
     */
    private static WorldGuardPlugin plugin = ExternalPlugins.getWorldGuard();

    /**
     * 与えられた {@link World} の {@link Player} の最大保護可能数を取得します.
     *
     * @param player 最大保護可能数を取得したい {@link Player} ({@code null} は許容されない)
     * @param world  最大保護可能数を取得したい {@link World} ({@code null} は許容されない)
     * @return {@link Player} の {@link World} における最大保護可能数
     */
    public static int getMaxRegionCount(@NotNull Player player, @NotNull World world) {
        final WorldConfiguration worldConfiguration = plugin.getGlobalStateManager().get(world);
        return worldConfiguration.getMaxRegionCount(player);
    }

    /**
     * 現在{@link Player}が{@link World}でオーナーになっている保護の数を返す。
     * @param who 誰か
     * @param where どのワールドか
     * @return オーナーになっている保護の数。どこのオーナーでもない場合は0
     */
    public static int getNumberOfRegions(@NotNull Player who, @NotNull World where) {
        return WorldGuardPlugin.inst().getRegionContainer().get(where).getRegionCountOfPlayer(WorldGuardPlugin.inst().wrapPlayer(who));
    }

    /**
     * 現在{@link Player}が{@link Location}の座標でOwnerになっている保護があるかどうかを返す。
     * @param player 調べる対象であるPlayer
     * @param where どの座標か
     * @return Ownerである保護があればtrue、ないか保護が2個以上重なっていて判定できなければfalse
     */
    public static boolean isRegionOwner(@NotNull Player player, @NotNull Location blockLoc) {
        Set<ProtectedRegion> regions = plugin.getRegionManager(player.getWorld()).getApplicableRegions(blockLoc).getRegions();
        if (regions.size() != 1) {return false;}
        return regions.iterator().next().isOwner(plugin.wrapPlayer(player));
    }
}
