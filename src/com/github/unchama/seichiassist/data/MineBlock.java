package com.github.unchama.seichiassist.data;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;

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
		int sum = 0;
		for(Material m : SeichiAssist.materiallist){
			sum += player.getStatistic(Statistic.MINE_BLOCK, m);
		}
		return  sum;
	}
}
