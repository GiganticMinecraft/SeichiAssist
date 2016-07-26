package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.Rank;




public class PlayerData {
	//エフェクトのフラグ
	public boolean effectflag;
	//内訳メッセージを出すフラグ
	public boolean messageflag;
	//1分間のデータを保存するincrease:１分間の採掘量
	public MineBlock minuteblock;
	//３０分間のデータを保存する．
	public MineBlock halfhourblock;
	//ガチャの基準となるポイント
	public int gachapoint;
	//最後のガチャポイントデータ
	public int lastgachapoint;
	//今回の採掘速度上昇レベルを格納
	public int minespeedlv;
	//持ってるポーションエフェクト全てを格納する．
	public List<EffectData> effectdatalist;
	//現在のプレイヤーのランク
	public int rank;
	//プレイヤーが獲得可能なpassive,activeスキルの数
	public int cangetpassiveskill;
	public int cangetactiveskill;
	//プレイヤーの持つアクティブスキル番号を格納する.
	public List<Integer> activeskills;
	//プレイヤーの持つパッシブスキル番号を格納する．
	public List<Integer> passiveskills;


	public PlayerData(){
		effectflag = true;
		messageflag = false;
		minuteblock = new MineBlock();
		halfhourblock = new MineBlock();
		effectdatalist = new ArrayList<EffectData>();
		gachapoint = 0;
		lastgachapoint = 0;
		minespeedlv = 0;
		minuteblock.before = 0;
		halfhourblock.before = 0;
		rank = 1;
		cangetpassiveskill = 0;
		cangetactiveskill = 0;
		activeskills = new ArrayList<Integer>();
		passiveskills = new ArrayList<Integer>(Arrays.asList(0,1));
		minespeedlv = 0;
		minuteblock.before = 0;
		halfhourblock.before = 0;

	}


	public void updata(Player p) {
		//破壊量データ(before)を設定
		minuteblock.before = MineBlock.calcMineBlock(p);
		halfhourblock.before = MineBlock.calcMineBlock(p);
		//プレイヤーのランクを計算し取得
		int rank = Rank.calcplayerRank(p);
		//ランクによるディスプレイネームを設定
		Rank.setDisplayName(rank,p);

	}

}
