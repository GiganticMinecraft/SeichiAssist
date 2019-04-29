package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.SeichiAssist;

public class MineStack {
	private int[] nums;

	public MineStack(){
		nums = new int[SeichiAssist.minestacklist.size()];
	}
	public int getNum(int idx){
		return nums[idx];
	}
	public void setNum(int idx, int num){
		nums[idx]=num;
	}
	public void addNum(int idx, int num){
		nums[idx]=nums[idx]+num;
	}
}
