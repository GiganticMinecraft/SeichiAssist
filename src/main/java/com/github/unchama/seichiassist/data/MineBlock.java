package com.github.unchama.seichiassist.data;


public class MineBlock{
	public long after;
	public long before;
	public long increase;

	MineBlock(){
		after = 0;
		before = 0;
		increase = 0;
	}
	public void setIncrease(){
		increase = after - before;
	}
}
