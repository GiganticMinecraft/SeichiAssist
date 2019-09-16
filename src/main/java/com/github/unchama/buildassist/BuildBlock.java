/*** Eclipse Class Decompiler plugin, copyright (c) 2012 Chao Chen (cnfree2000@hotmail.com) ***/
package com.github.unchama.buildassist;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class BuildBlock {
	public int after;
	public int before;
	public int increase;

	BuildBlock() {
		this.after = 0;
		this.before = 0;
		this.increase = 0;
	}

	public void setIncrease() {
		this.increase = (this.after - this.before);
	}

	public static BigDecimal calcBuildBlock(Player player) {
		BigDecimal sum = BigDecimal.ZERO;
		for (Material m : BuildAssist.materiallist) {
			sum = new BigDecimal(player.getStatistic(Statistic.USE_ITEM, m));
		}
		return sum;
	}
}
