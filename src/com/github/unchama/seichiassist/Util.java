package com.github.unchama.seichiassist;

import java.math.BigDecimal;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Util {
	//統計の総ブロック破壊数を出力する。
	public static int calcMineBlock(Player player){
		return  (int)player.getStatistic(Statistic.MINE_BLOCK, Material.STONE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.NETHERRACK)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.NETHER_BRICK)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.DIRT)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.GRAVEL)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.LOG)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.LOG_2)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.GRASS)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.COAL_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.IRON_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.GOLD_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.DIAMOND_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.LAPIS_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.EMERALD_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.REDSTONE_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.SAND)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.SANDSTONE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.QUARTZ_ORE)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.END_BRICKS)
				  + (int)player.getStatistic(Statistic.MINE_BLOCK, Material.ENDER_STONE);
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
	public static boolean isPlayerContainItem(Player player,ItemStack itemstack){
		return player.getInventory().contains(itemstack);
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
