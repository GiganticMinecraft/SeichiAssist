/*** Eclipse Class Decompiler plugin, copyright (c) 2012 Chao Chen (cnfree2000@hotmail.com) ***/
package com.github.unchama.buildassist;

import java.math.BigDecimal;

import com.github.unchama.seichiassist.MineStackObjectList;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.minestack.MineStackObj;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.jetbrains.annotations.Nullable;

public class Util {
	public Util() {
	}

	public static int toInt(String s) {
		return Integer.parseInt(s);
	}

	public static String getName(Player p) {
		return p.getName().toLowerCase();
	}

	public static String getName(String name) {
		return name.toLowerCase();
	}

	//ワールドガードAPIを返す
	public static WorldGuardPlugin getWorldGuard() {
		Plugin plugin = BuildAssist.plugin.getServer().getPluginManager().getPlugin("WorldGuard");

		// WorldGuard may not be loaded
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null; // Maybe you want throw an exception instead
		}

		return (WorldGuardPlugin) plugin;
	}

	//スキルの発動可否の処理(発動可能ならtrue、発動不可ならfalse)
	public static boolean isSkillEnable(Player player){
		//デバッグモード時は全ワールドでスキル使用を許可する(DEBUGWORLDNAME = worldの場合)
		String worldname = SeichiAssist.Companion.getSEICHIWORLDNAME();
		if(SeichiAssist.Companion.getDEBUG()){
			worldname = SeichiAssist.Companion.getDEBUGWORLDNAME();
		}
		//プレイヤーの場所が各種整地ワールド(world_SWで始まるワールド)または各種メインワールド(world)または各種TTワールドにいる場合
		if(player.getWorld().getName().toLowerCase().startsWith(worldname)
				|| player.getWorld().getName().equalsIgnoreCase("world")
				|| player.getWorld().getName().equalsIgnoreCase("world_2")
				|| player.getWorld().getName().equalsIgnoreCase("world_nether")
				|| player.getWorld().getName().equalsIgnoreCase("world_the_end")
				|| player.getWorld().getName().equalsIgnoreCase("world_TT")
				|| player.getWorld().getName().equalsIgnoreCase("world_nether_TT")
				|| player.getWorld().getName().equalsIgnoreCase("world_the_end_TT")
				|| player.getWorld().getName().equalsIgnoreCase("world_dot")
				){
			return true;
		}
		//それ以外のワールドの場合
		return false;
	}

	//設置ブロックカウント対象ワールドかを確認(対象ならtrue、対象外ならfalse)
	public static boolean isBlockCount(Player player){
		//デバッグモード時は全ワールドでスキル使用を許可する(DEBUGWORLDNAME = worldの場合)
		if(SeichiAssist.Companion.getDEBUG()){
			return true;
		}
		//プレイヤーの場所がメインワールド(world)または各種整地ワールド(world_SW)にいる場合
		if(player.getWorld().getName().toLowerCase().startsWith(SeichiAssist.Companion.getSEICHIWORLDNAME())
			|| player.getWorld().getName().equalsIgnoreCase("world")
			|| player.getWorld().getName().equalsIgnoreCase("world_2")
			|| player.getWorld().getName().equalsIgnoreCase("world_nether")
			|| player.getWorld().getName().equalsIgnoreCase("world_the_end")
            || player.getWorld().getName().equalsIgnoreCase("world_dot")
		){
			return true;
		}
		//それ以外のワールドの場合
		return false;
	}

	/**
	 * 指定した名前のマインスタックオブジェクトを返す
	 */
	// TODO これはここにあるべきではない
	@Deprecated public static @Nullable
	MineStackObj findMineStackObjectByName(String name) {
		return MineStackObjectList.INSTANCE.getMinestacklist().stream()
				.filter(obj -> name.equals(obj.getMineStackObjName()))
				.findFirst().orElse(null);
	}
	/**
	 * 1分間の設置料を指定量増加させます。
	 * ワールドによって倍率も加味されます。
	 *
	 * @param player 増加させるプレイヤー
	 * @param amount 増加量
	 */
	public static void addBuild1MinAmount(Player player, BigDecimal amount) {
		//プレイヤーデータ取得
		PlayerData playerData = BuildAssist.playermap.get(player.getUniqueId());
		//player.sendMessage("足す数:" + amount.doubleValue() + ",かけた後:" + amount.multiply(new BigDecimal("0.1")).doubleValue());
		//ワールドによって倍率変化
		if (player.getWorld().getName().toLowerCase().startsWith(SeichiAssist.Companion.getSEICHIWORLDNAME())) {
			playerData.build_num_1min = playerData.build_num_1min.add(amount.multiply(new BigDecimal("0.1")));
		} else {
			playerData.build_num_1min= playerData.build_num_1min.add(amount);
		}
	}
}
