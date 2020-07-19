package com.github.unchama.buildassist.util;

import com.github.unchama.buildassist.BuildAssist;
import com.github.unchama.buildassist.data.PlayerData;
import com.github.unchama.seichiassist.ManagedWorld;
import org.bukkit.entity.Player;
import scala.Option;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

@Deprecated
public final class Util {
    private Util() {
    }

    /**
     * プレイヤーの居るワールドでスキルが発動できるか判定する
     *
     * @param player 対象となるプレイヤー
     * @return 発動できる場合はtrue、できない場合はfalse
     */
    public static boolean isSkillEnable(final Player player) {
        //プレイヤーの場所が各種整地ワールド(world_SWで始まるワールド)または各種メインワールド(world)または各種TTワールドにいる場合
        Option<ManagedWorld> option = ManagedWorld.fromBukkitWorld(player.getWorld());
        if (option.nonEmpty()) {
            final ManagedWorld unwrapped = option.get();
            return ManagedWorld.ManagedWorldOps(unwrapped).isSeichi()
                    || Stream.of("world", "world_2", "world_nether", "world_the_end",
                                     "world_TT", "world_nether_TT", "world_the_end_TT", "world_dot")
                    .map(ManagedWorld::fromName)
                    .filter(Option::nonEmpty)
                    .map(Option::get)
                    .anyMatch(unwrapped::equals);
        } else {
            return false;
        }
    }

    /**
     * ブロックがカウントされるワールドにプレイヤーが居るか判定する
     *
     * @param player 対象のプレイヤー
     * @return いる場合はtrue、いない場合はfalse
     */
    public static boolean inTrackedWorld(final Player player) {
        //プレイヤーの場所がメインワールド(world)または各種整地ワールド(world_SW)にいるかどうか
        Option<ManagedWorld> option = ManagedWorld.fromBukkitWorld(player.getWorld());
        if (option.nonEmpty()) {
            final ManagedWorld unwrapped = option.get();
            return ManagedWorld.ManagedWorldOps(unwrapped).isSeichi()
                    || Stream.of("world", "world_2", "world_nether", "world_the_end", "world_dot")
                            .map(ManagedWorld::fromName)
                            .filter(Option::nonEmpty)
                            .map(Option::get)
                            .anyMatch(unwrapped::equals);
        } else {
            return false;
        }
    }

    /**
     * 1分間の設置量を指定量増加させます。
     * ワールドによって倍率も加味されます。
     *
     * @param player 増加させるプレイヤー
     * @param amount 増加量
     */
    public static void increaseBuildCount(final Player player, final BigDecimal amount) {
        final PlayerData playerData = BuildAssist.playermap().get(player.getUniqueId()).get();
        final ManagedWorld mw = ManagedWorld.fromBukkitWorld(player.getWorld())
                .getOrElse(() -> { throw new NoSuchElementException("Fatal: World " + player.getWorld() + " is not managed"); });
        // 整地ワールドならx0.1
        final BigDecimal finalAmount = ManagedWorld.ManagedWorldOps(mw).isSeichi()
                ? amount.movePointRight(1)
                : amount;
        playerData.buildCountBuffer = playerData.buildCountBuffer.add(finalAmount);
    }
}
