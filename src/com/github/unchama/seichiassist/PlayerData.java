package com.github.unchama.seichiassist;

import java.util.ArrayList;
import java.util.List;




public class PlayerData {
	boolean firstjoinflag;
	boolean onlineflag;
	MineBlock minuteblock;
	List<EffectData> effectdatalist;
	PlayerData(){
		firstjoinflag = false;
		onlineflag = false;
		minuteblock = new MineBlock();
		effectdatalist = new ArrayList<EffectData>();
	}
}
