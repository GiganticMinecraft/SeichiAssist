package com.github.unchama.seichiassist.data;

public class RankData {
	public String name;
	public int level;
	public long totalbreaknum;
	//追加ランキングデータ
	public int playtick;
	public int p_vote;
	public int premiumeffectpoint;

	public RankData(){
		name = null;
		level = 1;
		totalbreaknum = 0;
	}
}
