package com.github.unchama.buildassist.util;

import com.github.unchama.buildassist.BuildAssist;
import com.github.unchama.buildassist.data.PlayerData;
import com.github.unchama.seichiassist.MineStackObjectList;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.minestack.MineStackObj;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public final class Util {
    private Util() {
    }

    public static String getName(final Player p) {
        return p.getName().toLowerCase();
    }

    /**
     * プレイヤーの居るワールドでスキルが発動できるか判定する
     *
     * @param player 対象となるプレイヤー
     * @return 発動できる場合はtrue、できない場合はfalse
     */
    public static boolean isSkillEnable(final Player player) {
        //デバッグモード時は全ワールドでスキル使用を許可する(DEBUGWORLDNAME = worldの場合)
        String worldname = SeichiAssist.SEICHIWORLDNAME();
        if (SeichiAssist.DEBUG()) {
            worldname = SeichiAssist.DEBUGWORLDNAME();
        }
        //プレイヤーの場所が各種整地ワールド(world_SWで始まるワールド)または各種メインワールド(world)または各種TTワールドにいる場合
        // TODO: ManagedWorldへ移行
        final String name = player.getWorld().getName();
        return name.toLowerCase().startsWith(worldname)
                || name.equalsIgnoreCase("world")
                || name.equalsIgnoreCase("world_2")
                || name.equalsIgnoreCase("world_nether")
                || name.equalsIgnoreCase("world_the_end")
                || name.equalsIgnoreCase("world_TT")
                || name.equalsIgnoreCase("world_nether_TT")
                || name.equalsIgnoreCase("world_the_end_TT")
                || name.equalsIgnoreCase("world_dot");
        //それ以外のワールドの場合
    }

    /**
     * ブロックがカウントされるワールドにプレイヤーが居るか判定する
     *
     * @param player 対象のプレイヤー
     * @return いる場合はtrue、いない場合はfalse
     */
    public static boolean inTrackedWorld(final Player player) {
        //デバッグモード時は全ワールドでスキル使用を許可する
        if (SeichiAssist.DEBUG()) {
            return true;
        }
        final String name = player.getWorld().getName();
        //プレイヤーの場所がメインワールド(world)または各種整地ワールド(world_SW)にいるかどうか
        // TODO: ManagedWorldへ移行
        return name.toLowerCase().startsWith(SeichiAssist.SEICHIWORLDNAME())
                || name.equalsIgnoreCase("world")
                || name.equalsIgnoreCase("world_2")
                || name.equalsIgnoreCase("world_nether")
                || name.equalsIgnoreCase("world_the_end")
                || name.equalsIgnoreCase("world_dot");
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
        // 整地ワールドならx0.1
        // TODO: ManagedWorldへ移行
        final BigDecimal finalAmount = player.getWorld().getName().toLowerCase().startsWith(SeichiAssist.SEICHIWORLDNAME())
                ? amount.multiply(new BigDecimal("0.1"))
                : amount;
        playerData.buildCountBuffer = playerData.buildCountBuffer.add(finalAmount);
    }
}
