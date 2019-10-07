package com.github.unchama.buildassist;

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

    public static int toInt(final String s) {
        return Integer.parseInt(s);
    }

    public static String getName(final Player p) {
        return p.getName().toLowerCase();
    }

    public static String getName(final String name) {
        return name.toLowerCase();
    }

    //ワールドガードAPIを返す
    public static WorldGuardPlugin getWorldGuard() {
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (!(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
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
        //デバッグモード時は全ワールドでスキル使用を許可する(DEBUGWORLDNAME = worldの場合)
        if (SeichiAssist.DEBUG()) {
            return true;
        }
        final String name = player.getWorld().getName();
        //プレイヤーの場所がメインワールド(world)または各種整地ワールド(world_SW)にいる場合
        return name.toLowerCase().startsWith(SeichiAssist.SEICHIWORLDNAME())
                || name.equalsIgnoreCase("world")
                || name.equalsIgnoreCase("world_2")
                || name.equalsIgnoreCase("world_nether")
                || name.equalsIgnoreCase("world_the_end")
                || name.equalsIgnoreCase("world_dot");
        //それ以外のワールドの場合
    }

    /**
     * 指定した名前のマインスタックオブジェクトを返す
     */
    // FIXME: これはここにあるべきではない
    @Deprecated
    public static @Nullable
    MineStackObj findMineStackObjectByName(final String name) {
        return MineStackObjectList.minestacklist()
                .filter((MineStackObj obj) -> name.equals(obj.mineStackObjName()))
                .headOption().getOrElse(() -> null);
    }

    /**
     * 1分間の設置量を指定量増加させます。
     * ワールドによって倍率も加味されます。
     *
     * @param player 増加させるプレイヤー
     * @param amount 増加量
     */
    public static void addBuild1MinAmount(final Player player, final BigDecimal amount) {
        //プレイヤーデータ取得
        final PlayerData playerData = BuildAssist.playermap().get(player.getUniqueId()).get();
        //player.sendMessage("足す数:" + amount.doubleValue() + ",かけた後:" + amount.multiply(new BigDecimal("0.1")).doubleValue());
        //ワールドによって倍率変化
        if (player.getWorld().getName().toLowerCase().startsWith(SeichiAssist.SEICHIWORLDNAME())) {
            playerData.build_num_1min = playerData.build_num_1min.add(amount.multiply(new BigDecimal("0.1")));
        } else {
            playerData.build_num_1min = playerData.build_num_1min.add(amount);
        }
    }
}
