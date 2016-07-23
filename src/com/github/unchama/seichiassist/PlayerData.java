package com.github.unchama.seichiassist;

import java.util.ArrayList;
import java.util.List;




public class PlayerData {
	//初めて参加した時のフラグ
	boolean firstjoinflag;
	//オンラインの時のフラグ
	boolean onlineflag;
	//エフェクトのフラグ
	boolean effectflag;
	//1分間のデータを保存するincrease:１分間の採掘量
	MineBlock minuteblock;
	//３０分間のデータを保存する．
	MineBlock halfhourblock;
	//ガチャの基準となるポイント(beforeとafterのみ使用）
	MineBlock gachapoint;
	//採掘速度のみの合計値を保存
	int allamplifier;
	//持ってるポーションエフェクト全てを格納する．
	List<EffectData> effectdatalist;
	
	PlayerData(){
		firstjoinflag = false;
		onlineflag = false;
		effectflag = true;
		minuteblock = new MineBlock();
		effectdatalist = new ArrayList<EffectData>();
		gachapoint = new MineBlock();
		allamplifier = 0;
	}
}
