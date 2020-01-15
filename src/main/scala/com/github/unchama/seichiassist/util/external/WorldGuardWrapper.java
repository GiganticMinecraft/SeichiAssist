package com.github.unchama.seichiassist.util.external;

import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.World;
import org.bukkit.entity.Player;
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
}
