package com.github.unchama.seichiassist.data;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class MineBlock{
	public int after;
	public int before;
	public int increase;

	MineBlock(){
		after = 0;
		before = 0;
		increase = 0;
	}
	public void setIncrease(){
		increase = after - before;
	}
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
}
