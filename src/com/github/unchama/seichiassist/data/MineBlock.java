package com.github.unchama.seichiassist.data;


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
}
