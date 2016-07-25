package com.github.unchama.seichiassist;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class Util {
	//統計の総ブロック破壊数を出力する。
	public static int calcMineBlock(Player player){
		return  player.getStatistic(Statistic.MINE_BLOCK, Material.STONE)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.NETHERRACK)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.NETHER_BRICK)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.DIRT)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.GRAVEL)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.LOG)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.LOG_2)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.GRASS)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.COAL_ORE)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.IRON_ORE)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.GOLD_ORE)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.DIAMOND_ORE)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.LAPIS_ORE)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.EMERALD_ORE)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.REDSTONE_ORE)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.SAND)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.SANDSTONE)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.QUARTZ_ORE)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.END_BRICKS)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.ENDER_STONE)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.ICE)
				  + player.getStatistic(Statistic.MINE_BLOCK, Material.PACKED_ICE)
;
	}

	public static String calcplayerRank(Player player){

		int mines = 0;
		String name = player.getDisplayName();
		List<Integer> ranklist = new ArrayList<Integer>(Arrays.asList(
				15,49,106,198,333,
				705,1265,2105,3347,4589,
				5831,7073,8315,9557,11047,
				12835,14980,17554,20642,24347,
				28793,34128,40530,48212,57430,
				68491,81764,97691,116803,135915,//30
				155027,174139,193251,212363,235297,
				262817,295841,335469,383022,434379,
				489844,549746,614440,684309,759767,
				841261,929274,1024328,1126986,1237856,
				1357595,1486913,1626576,1777412,1940314,
				2116248,2306256,2511464,2733088,2954712,//60
				3176336,3397960,3619584,3841208,4080561,
				4339062));
		mines = calcMineBlock(player);
		//比較対象はリスト
		int i;
		for(i = 0 ; ranklist.get(i).intValue() <= mines ;i++){
		}
		name = "[Lv" + (i+1) + "]" + player.getName();
		SeichiAssist.playermap.get(name).rank = (i+1);
		return name;
	}

	public static ItemStack getskull(){
		ItemStack skull;
		SkullMeta skullmeta;
		skull = new ItemStack(Material.SKULL_ITEM, 1);
		skullmeta = (SkullMeta) skull.getItemMeta();
		skull.setDurability((short) 3);
		skullmeta.setDisplayName("ガチャ券");
		skullmeta.setOwner("unchama");
		skull.setItemMeta(skullmeta);
		return skull;
	}
	public static int getOnlinePlayer(){
		return Bukkit.getOnlinePlayers().size();
	}
	public static double toDouble(String s){
		return Double.parseDouble(s);
	}
	public static int toInt(String s) {
		return Integer.parseInt(s);
	}
	public static double Decimal(double d) {
		BigDecimal bi = new BigDecimal(String.valueOf(d));
		return bi.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	public static boolean isPlayerInventryEmpty(Player player){
		return (player.getInventory().firstEmpty()== -1);
	}
	public static void dropItem(Player player,ItemStack itemstack){
		player.getWorld().dropItemNaturally(player.getLocation(), itemstack);
	}
	public static void addItem(Player player,ItemStack itemstack){
		player.getInventory().addItem(itemstack);
	}
	public static void sendEveryMessage(String str){
		SeichiAssist plugin = SeichiAssist.plugin;
		for ( Player player : plugin.getServer().getOnlinePlayers() ) {
			player.sendMessage(str);
		}
	}
	public static void sendEverySound(Sound str, float a, float b){
		SeichiAssist plugin = SeichiAssist.plugin;
		for ( Player player : plugin.getServer().getOnlinePlayers() ) {
			player.playSound(player.getLocation(), str, a, b);
		}
	}
}
