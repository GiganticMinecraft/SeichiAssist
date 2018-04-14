package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.SeichiAssist;

public class MineStack {
	private int[] nums;
	/*
	public int stone;
	public int dirt;
	public int gravel;
	public int sand;
	public int sandstone;
	public int ender_stone;
	public int obsidian;
	public int cobblestone;
	public int netherrack;
	public int grass;
	public int quartz;
	public int quartz_ore;
	public int soul_sand;
	public int magma;
	public int coal_ore;
	public int iron_ore;
	public int coal;
	public int packed_ice;
	public int gold_ore;
	public int lapis_ore;
	public int emerald_ore;
	public int redstone_ore;
	public int diamond_ore;
	public int log;
	public int log_2;
	public int wood;
	public int hard_clay;
	public int stained_clay;
	public int fence;
	public int lapis_lazuli;
	//public int ink_sack4;
	public int emerald; //追加
	public int redstone; //追加
	public int diamond; //追加
	public int granite; //追加
	//public int stone1;
	public int diorite; //追加
	//public int stone3;
	public int andesite; //追加
	//public int stone5;
	public int red_sand; //追加
	//public int sand1; //追加
	public int red_sandstone; //追加
	public int log1; //追加
	public int log2; //追加
	public int log3; //追加
	public int log_21; //追加
	public int stained_clay1; //追加
	public int stained_clay4; //追加
	public int stained_clay8; //追加
	public int stained_clay12; //追加
	public int stained_clay14; //追加
	public int clay; //追加
	public int glowstone; //追加
	public int dirt1;
	public int dirt2;
	public int mycel;
	public int snow_block;
	public int ice;
	public int wood5;
	public int dark_oak_fence;
	public int mossy_cobblestone;
	public int rails;
	public int leaves;
	public int leaves1;
	public int leaves2;
	public int leaves3;
	public int leaves_2;
	public int leaves_21;
	public int sapling;
	public int sapling1;
	public int sapling2;
	public int sapling3;
	public int sapling4;
	public int sapling5;
	public int exp_bottle;
	public int huge_mushroom_1;
	public int huge_mushroom_2;
	public int web;
	public int string;
	public int gachaimo; //がちゃりんごテスト用
	*/

	public MineStack(){
		nums = new int[SeichiAssist.minestacklist.size()];
		/*
		stone = 0;
		dirt = 0;
		gravel = 0;
		sand = 0;
		sandstone = 0;
		ender_stone = 0;
		obsidian = 0;
		cobblestone = 0;
		netherrack = 0;
		grass = 0;
		quartz = 0;
		quartz_ore = 0;
		soul_sand = 0;
		magma = 0;
		coal_ore = 0;
		iron_ore = 0;
		coal = 0;
		packed_ice = 0;
		gold_ore = 0;
		lapis_ore = 0;
		emerald_ore = 0;
		redstone_ore = 0;
		diamond_ore = 0;
		log = 0;
		log_2 = 0;
		wood = 0;
		hard_clay = 0;
		stained_clay = 0;
		fence = 0;
		lapis_lazuli = 0;
		emerald = 0; //追加
		redstone = 0; //追加
		diamond = 0; //追加
		granite = 0; //追加
		diorite = 0; //追加
		andesite = 0; //追加
		red_sand = 0; //追加
		red_sandstone = 0; //追加
		log1 = 0; //追加
		log2 = 0; //追加
		log3 = 0; //追加
		log_21 = 0; //追加
		stained_clay1 = 0; //追加
		stained_clay4 = 0; //追加
		stained_clay8 = 0; //追加
		stained_clay12 = 0; //追加
		stained_clay14 = 0; //追加
		clay = 0; //追加
		glowstone = 0; //追加
		dirt1 = 0;
		dirt2 = 0;
		mycel = 0;
		snow_block = 0;
		ice = 0;
		wood5 = 0;
		dark_oak_fence = 0;
		mossy_cobblestone = 0;
		rails = 0;
		leaves = 0;
		leaves1 = 0;
		leaves2 = 0;
		leaves3 = 0;
		leaves_2 = 0;
		leaves_21 = 0;
		sapling = 0;
		sapling1 = 0;
		sapling2 = 0;
		sapling3 = 0;
		sapling4 = 0;
		sapling5 = 0;
		exp_bottle = 0;
		huge_mushroom_1 = 0;
		huge_mushroom_2 = 0;
		web = 0;
		string = 0;
		gachaimo = 0;
		*/
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
