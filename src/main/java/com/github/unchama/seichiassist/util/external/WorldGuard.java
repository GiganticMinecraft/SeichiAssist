package com.github.unchama.seichiassist.util.external;

import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

/**
 * WorldGuardの各種関数を集めたクラスです.
 *
 * @author karayuu
 */
public class WorldGuard {
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
    public static int getMaxRegionCount(@Nonnull Player player, @Nonnull World world) {
        final WorldConfiguration worldConfiguration = plugin.getGlobalStateManager().get(requireNonNull(world));
        return worldConfiguration.getMaxRegionCount(requireNonNull(player));
    }
}
