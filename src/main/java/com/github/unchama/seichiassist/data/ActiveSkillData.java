package com.github.unchama.seichiassist.data;

import java.util.HashMap;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.task.AssaultTaskRunnable;

public class ActiveSkillData {
	SeichiAssist plugin = SeichiAssist.plugin;
	//アクティブスキルポイント
	public int skillpoint;
	//アクティブスキルエフェクトポイント
	public int effectpoint;
	//プレミアムアクティブスキルエフェクトポイント
	public int premiumeffectpoint;
	//投てきスキル獲得量
	public int arrowskill;
	//連続破壊スキル獲得量
	public int multiskill;
	//破壊スキル獲得量
	public int breakskill;
	//凝固スキル獲得量
	public int fluidcondenskill;
	//水凝固スキル獲得量
	public int watercondenskill;
	//熔岩凝固スキル獲得量
	public int lavacondenskill;
	//アクティブスキルの種類番号を格納
	public int skilltype;
	//アサルトスキルの種類番号を格納
	public int assaulttype;
	//選択されているアクティブスキルの番号を格納
	public int skillnum;
	//選択されているアサルトスキルの番号を格納
	public int assaultnum;
	//スキルクールダウン用フラグ
	public boolean skillcanbreakflag;
	//採掘用アクティブスキルのフラグ 0:なし 1:上破壊 2:下破壊
	public int mineflagnum;
	//アサルトスキル.コンデンススキルのtask
	public BukkitTask assaulttask;
	//自然マナ回復のtask
	public BukkitTask manaregenetask;
	//アサルトスキルのフラグ
	public boolean assaultflag;
	//エフェクトの獲得フラグリスト<エフェクト番号,エフェクト獲得フラグ>
	public Map<Integer,Boolean> effectflagmap;
	//スペシャルエフェクトの獲得フラグリスト<エフェクト番号,エフェクト獲得フラグ>
	public Map<Integer,Boolean> premiumeffectflagmap;
	//スペシャルエフェクトを使用するフラグ
	public boolean specialflag;
	//選択されているアクティブスキルの番号を格納
	public int effectnum;
	//通常スキルで破壊されるエリア
	public BreakArea area;
	//アサルトスキルで破壊されるエリア
	public BreakArea assaultarea;

	//マナクラス
	public Mana mana;

	public ActiveSkillData(){
		mineflagnum = 0;
		assaultflag = false;
		assaulttask = null;
		specialflag = false;
		skilltype = 0;
		skillnum = 0;
		skillcanbreakflag = true;
		skillpoint = 0;
		effectpoint = 0;
		premiumeffectpoint = 0;
		arrowskill = 0;
		multiskill = 0;
		breakskill = 0;
		watercondenskill = 0;
		lavacondenskill = 0;
		fluidcondenskill = 0;
		effectnum = 0;
		effectflagmap = new HashMap<Integer,Boolean>();
		premiumeffectflagmap = new HashMap<Integer,Boolean>();

		ActiveSkillEffect[] activeskilleffect = ActiveSkillEffect.values();
		for(int i=0 ; i < activeskilleffect.length ; i++){
			effectflagmap.put(activeskilleffect[i].getNum(), false);
		}
		ActiveSkillPremiumEffect[] activeskillpremiumeffect = ActiveSkillPremiumEffect.values();
		for(int i=0 ; i < activeskillpremiumeffect.length ; i++){
			premiumeffectflagmap.put(activeskillpremiumeffect[i].getNum(), false);
		}
        area = null;
		assaultarea = null;

		mana = new Mana();
	}
	//activeskillpointをレベルに従って更新
	public void updataActiveSkillPoint(Player player,int level) {
		int point = 0;
		//レベルに応じたスキルポイント量を取得
		for(int i = 1;i <= level;i++){
			point += i / 10 + 1;
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
		//水凝固スキル
		for(int i = watercondenskill; i >= 7 ; i--){
			point -= i * 10;
		}
		//熔岩凝固スキル
		for(int i = lavacondenskill; i >= 7 ; i--){
			point -= i * 10;
		}
		if (fluidcondenskill == 10){
			point -= 110;
		}

		if(SeichiAssist.DEBUG){
			player.sendMessage("獲得済みスキルを考慮したアクティブスキルポイント：" + point);
			point += 10000;
		}
		if(point < 0){
			reset();
			player.sendMessage("アクティブスキルポイントが負の値となっていたため、リセットしました。");
			updataActiveSkillPoint(player,level);
		}else{
			skillpoint = point;
		}
	}

	public void reset() {
		//タスクを即終了
		if(assaultflag)try{this.assaulttask.cancel();}catch(NullPointerException e){}

		//初期化
		arrowskill = 0;
		multiskill = 0;
		breakskill = 0;
		watercondenskill = 0;
		lavacondenskill = 0;
		fluidcondenskill = 0;
		skilltype = 0;
		skillnum = 0;
		assaulttype = 0;
		assaultnum = 0;

		assaultflag = false;

	}
	public void RemoveAllTask() {
		try{assaulttask.cancel();}catch(NullPointerException e){}
	}
	public void updataSkill(Player player ,int type, int skilllevel ,int mineflagnum) {
		this.skilltype = type;
		this.skillnum = skilllevel;
		this.mineflagnum = mineflagnum;
		//スキルが選択されていなければ終了
		if(skilltype == 0){
			return;
		}
		//スキルフラグがオンの時の処理
		if(mineflagnum != 0){
			this.area = new BreakArea(player,type,skilllevel,mineflagnum,false);
		}

	}
	public void updataAssaultSkill(Player player, int type, int skilllevel,int mineflagnum) {
		this.assaulttype = type;
		this.assaultnum = skilllevel;
		this.mineflagnum = mineflagnum;

		try{this.assaulttask.cancel();}catch(NullPointerException e){}
		//スキルが選択されていなければ終了
		if(assaulttype == 0){
			return;
		}
		//スキルフラグがオンの時の処理
		if(mineflagnum != 0){
			this.assaultarea = new BreakArea(player,type,skilllevel,mineflagnum,true);
			this.assaultflag = true;
			this.assaulttask = new AssaultTaskRunnable(player).runTaskTimer(plugin,10,10);
		}//オフの時の処理
		else{
			this.assaultflag = false;
		}
	}
	//スキル使用中の場合Taskを実行する
	public void runTask(Player player) {

		//アサルトスキルの実行
		if(this.assaultflag && this.assaulttype != 0){
			this.updataAssaultSkill(player,this.assaulttype,this.assaultnum,this.mineflagnum);
			String name = ActiveSkill.getActiveSkillName(this.assaulttype, this.assaultnum);
			player.sendMessage(ChatColor.LIGHT_PURPLE + "アサルトスキル:" + name + "  を選択しています。");
			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
		}

		//通常スキルの実行
		if(this.skilltype != 0){
			this.updataSkill(player, this.skilltype, this.skillnum,this.mineflagnum);
			String name = ActiveSkill.getActiveSkillName(this.skilltype, this.skillnum);
			player.sendMessage(ChatColor.GREEN + "アクティブスキル:" + name + "  を選択しています。");
			player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);
		}


	}
	public void clearSellect(Player player) {
		this.skilltype = 0;
		this.skillnum = 0;
		this.mineflagnum = 0;
		this.assaultnum = 0;
		this.assaulttype = 0;
		this.assaultflag = false;
		try{this.assaulttask.cancel();}catch(NullPointerException e){}
		player.sendMessage(ChatColor.GREEN + "全ての選択を削除しました。");
		player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float) 0.1);

	}
	public void updateonJoin(Player player, int level) {
		updataActiveSkillPoint(player, level);
		runTask(player);
		mana.update(player,level);
	}
	public void updateonQuit(Player player) {
		mana.removeBar();
	}
}
