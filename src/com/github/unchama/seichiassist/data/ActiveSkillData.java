package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.SeichiAssist;

public class ActiveSkillData {
	//アクティブスキルポイント
	public int skillpoint;
	//投てきスキル獲得量
	public int arrowskill;
	//連続破壊スキル獲得量
	public int multiskill;
	//破壊スキル獲得量
	public int breakskill;
	//凝固スキル獲得量
	public int condenskill;
	//アクティブスキルの種類番号を格納
	public int skilltype;
	//選択されているアクティブスキルの番号を格納
	public int skillnum;
	//スキルクールダウン用フラグ
	public boolean skillcanbreakflag;
	//採掘用アクティブスキルのフラグ 0:なし 1:上破壊 2:下破壊
	public int mineflagnum;
	//エフェクトの獲得フラグ
	public boolean effect_explosion;
	public boolean effect_blizzard;
	public boolean effect_meteo;
	//選択されているアクティブスキルの番号を格納
	public int effectnum;
	//スキルで破壊されるブロック
	public List<Block> blocklist;
	//凝固スキルを発動する時間
	public int explosiontime;
	//凝固スキルを発動し何かにあたったときの処理
	public boolean hitflag;

	public ActiveSkillData(){
		mineflagnum = 0;
		skilltype = 0;
		skillnum = 0;
		skillcanbreakflag = true;
		skillpoint = 0;
		arrowskill = 0;
		multiskill = 0;
		breakskill = 0;
		condenskill = 0;
		effectnum = 0;
		blocklist = new ArrayList<Block>();
		explosiontime = 1;
		hitflag = false;
	}
	//activeskillpointをレベルに従って更新
	public void updataActiveSkillPoint(Player player,int level) {
		int point = 0;
		//レベルに応じたスキルポイント量を取得
		for(int i = 1;i <= level;i++){
			point += (int)(i / 10) + 1;
		}
		if(SeichiAssist.DEBUG){
			player.sendMessage("あなたのレベルでの獲得アクティブスキルポイント：" + point);
		}
		//取得しているスキルを確認してその分のスキルポイントを引く
		//遠距離スキル
		for(int i = arrowskill; i >= 4 ; i--){
			point -= i * 10;
		}
		//マルチ破壊スキル
		for(int i = multiskill; i >= 4 ; i--){
			point -= i * 10;
		}
		//破壊スキル
		for(int i = breakskill; i >= 1 ; i--){
			point -= i * 10;
		}
		//凝固スキル
		for(int i = condenskill; i >= 4 ; i--){
			point -= i * 10;
		}

		//ここからエフェクトによる減算処理
		ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();

		for(int i = 0; i < skilleffect.length;i++){
			if(skilleffect[i].isObtained(this)){
				point -= skilleffect[i].getUsePoint();
			}
		}

		if(SeichiAssist.DEBUG){
			player.sendMessage("獲得済みスキルを考慮したアクティブスキルポイント：" + point);
			point += 10000;
		}
		if(point < 0){
			reset();
			player.sendMessage("アクティブスキルポイントが負の値となっていたため、リセットしました。");
		}else{
			skillpoint = point;
		}
	}
	public void reset() {
		arrowskill = 0;
		multiskill = 0;
		breakskill = 0;
		condenskill = 0;
		skilltype = 0;
		skillnum = 0;
		effect_explosion = false;
		effect_blizzard = false;
		effect_meteo = false;
		effectnum = 0;
	}
}
