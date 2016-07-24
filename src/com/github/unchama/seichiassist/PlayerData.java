package com.github.unchama.seichiassist;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;




public class PlayerData {
	//初めて参加した時のフラグ
	boolean firstjoinflag;
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
	//採掘速度上昇量を格納
	int minespeed;
	//独自プラグインの採掘速度上昇量を格納
	int seichispeed;
	//持ってるポーションエフェクト全てを格納する．
	List<EffectData> effectdatalist;

	PlayerData(Player player){
		firstjoinflag = false;
		effectflag = true;
		minuteblock = new MineBlock();
		halfhourblock = new MineBlock();
		effectdatalist = new ArrayList<EffectData>();
		gachapoint = new MineBlock();
		allamplifier = 0;
		minespeed = 0;
		seichispeed = 0;
		minuteblock.before = Util.calcMineBlock(player);
		halfhourblock.before = minuteblock.before;
	}
}
