package com.github.unchama.seichiassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;




public class PlayerData {
	//初めて参加した時のフラグ
	boolean firstjoinflag;
	//エフェクトのフラグ
	boolean effectflag;
	//内訳メッセージを出すフラグ
	boolean messageflag;
	//1分間のデータを保存するincrease:１分間の採掘量
	MineBlock minuteblock;
	//３０分間のデータを保存する．
	MineBlock halfhourblock;
	//ガチャの基準となるポイント
	int gachapoint;
	//最後のガチャポイントデータ
	int lastgachapoint;
	//今回の採掘速度上昇量を格納
	int amplifier;
	//持ってるポーションエフェクト全てを格納する．
	List<EffectData> effectdatalist;
	//現在のプレイヤーのランク
	int rank;
	//プレイヤーが獲得可能なpassive,activeスキルの数
	int cangetpassiveskill;
	int cangetactiveskill;
	//プレイヤーの持つアクティブスキル番号を格納する.
	List<Integer> activeskills;
	//プレイヤーの持つパッシブスキル番号を格納する．
	List<Integer> passiveskills;
	
	PlayerData(){
		firstjoinflag = false;
		effectflag = true;
		messageflag = false;
		minuteblock = new MineBlock();
		halfhourblock = new MineBlock();
		effectdatalist = new ArrayList<EffectData>();
		gachapoint = 0;
		lastgachapoint = 0;
		amplifier = 0;
		minuteblock.before = 0;
		halfhourblock.before = minuteblock.before;
		rank = 1;
		cangetpassiveskill = 0;
		cangetactiveskill = 0;
		activeskills = new ArrayList<Integer>();
		passiveskills = new ArrayList<Integer>(Arrays.asList(0,1));
	}
}
