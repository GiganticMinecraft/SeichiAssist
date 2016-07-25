package com.github.unchama.seichiassist;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;




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
	//表示される名前
	String displayname;
	//登録された時のプレイヤー型
	Player player;

	PlayerData(Player _player){
		firstjoinflag = false;
		effectflag = true;
		messageflag = false;
		minuteblock = new MineBlock();
		halfhourblock = new MineBlock();
		effectdatalist = new ArrayList<EffectData>();
		gachapoint = 0;
		lastgachapoint = 0;
		amplifier = 0;
		minuteblock.before = Util.calcMineBlock(_player);
		halfhourblock.before = minuteblock.before;
		displayname = _player.getDisplayName();
		player = _player;
	}
}
