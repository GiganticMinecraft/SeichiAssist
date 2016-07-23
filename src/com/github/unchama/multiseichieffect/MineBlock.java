package com.github.unchama.multiseichieffect;

import static com.github.unchama.multiseichieffect.Util.*;

import org.bukkit.entity.Player;

public class MineBlock{
	private int last_mineblock;
	private int now_mineblock;
	private int increase_mineblock;



	
	MineBlock(){
		last_mineblock = calcMineblock(player);
		now_mineblock = 0;
		increase_mineblock = 0;
	}



	public void setIncrease(){
		increase_mineblock = now_mineblock - last_mineblock;
	}
	public void setNow(){
		now_mineblock = calcMineblock(player);
	}
	public void setLast(){
		last_mineblock = now_mineblock;
	}
	public int getLast(){
		return last_mineblock;
	}
	public int getNow(){
		return now_mineblock;
	}
	public int getIncrease(){
		return increase_mineblock;
	}
}
