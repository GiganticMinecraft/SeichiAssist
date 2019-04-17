package com.github.unchama.seichiassist.data;



import java.util.UUID;

import com.github.unchama.seichiassist.util.TypeConverter;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;


public class Mana {
	//マナの値
	double m;
	//マックスの値
	private double max;
	//バークラス
	BossBar manabar;
	//読み込まれているかどうかのフラグ
	boolean loadflag;

	//引数なしのコンストラクタ
	public Mana() {
		this.m = 0;
		this.setMax(0);
		this.loadflag = false;
	}
	//必ず最初のみ実行してほしいメソッド
	public void update(Player player, int level) {
		//現在のレベルでのマナ上限値を計算しバーに表示
		//mの値は既に得られているはず。
		this.loadflag = true;
		this.calcMaxMana(player, level);
		displayMana(player,level);

	}
	//現在マナをマナバーに表示させる
	public void displayMana(Player player,int level){
		if(!loadflag)return;
		removeBar();
		setManaBar(player,level);
	}
	private void setManaBar(Player player,int level) {
		manabar = player.getServer().createBossBar(ChatColor.AQUA + "" + ChatColor.BOLD + "マナ(" + TypeConverter.Decimal(m) + "/" + getMax() + ")", BarColor.BLUE, BarStyle.SOLID);

		if(m/getMax() < 0.0 || m/getMax() > 1.0){
			reset(player, level);
			player.sendMessage(ChatColor.RED + "不正な値がマナとして保存されていたためリセットしました。");
		}
		if (!(getMax() == 0||getMax() < 0)){
		manabar.setProgress(m/getMax());
		manabar.addPlayer(player);
		}
	}
	private void reset(Player player, int level) {
		this.calcMaxMana(player, level);
		if(this.m < 0.0)this.m = 0;
		if(this.m > getMax())this.m = getMax();
	}
	//現在のバーを削除する（更新するときは不要）
	public void removeBar(){
		if (manabar != null) {
			manabar.removeAll();
		}
	}
	public void increaseMana(double i,Player player,int level){
		this.m += i;
		if(m > getMax()) m = getMax();
		displayMana(player, level);
	}
	public void decreaseMana(double d,Player player,int level){
		this.m -= d;
		if(m < 0) m = 0;
		if(SeichiAssist.DEBUG)m = getMax();
		displayMana(player,level);
	}
	public boolean hasMana(double h){
		return compare(m, h) >= 0;
	}

	private int compare(double x, double y) {
		if(x > y)return 1;
		if(x == y)return 0;
		return -1;
	}
	//レベルアップするときに実行したい関数
	public void LevelUp(Player player,int level){
		calcMaxMana(player, level);
		fullMana(player,level);
	}
	//マナ最大値を計算する処理
	public void calcMaxMana(Player player, int level){

		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//playerdataを取得
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);

		if(SeichiAssist.DEBUG){
			setMax(100000);
			return;
		}
		//レベルが10行っていない時レベルの値で処理を終了(修正:マナは0)
		if(level < 10){
			//this.max = level;
			this.setMax(0.0);
			return;
		}
		//１０行ってる時の処理
		double t_max = 100;
		int increase = 10;
		int inc_inc = 2;
		//１１以降の処理
		for(int i = 10 ; i < level;i++){
			if(i%10 == 0 && i<=110 && i != 10){
				increase += inc_inc;
				inc_inc *= 2;
			}
			t_max += increase;
		}
		//貢献度ptの上昇値
		t_max += playerdata.added_mana * SeichiAssist.config.getContributeAddedMana();

		this.setMax(t_max);
	}

	/**
	 * @param level
	 * レベル
	 *
	 * @return
	 * 最大マナ
	 */
	//マナ最大値を計算する処理
	public double calcMaxManaOnly(Player player, int level){

		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//playerdataを取得
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);

		/*
		if(SeichiAssist.DEBUG){
			max = 100000;
			return;
		}
		*/
		//レベルが10行っていない時レベルの値で処理を終了
		if(level < 10){
			//temp_max = level;
			setMax(0.0);
			return 0.0;
		}
		//１０行ってる時の処理
		double t_max = 100;
		int increase = 10;
		int inc_inc = 2;
		//１１以降の処理
		for(int i = 10 ; i < level;i++){
			if(i%10 == 0 && i<=110 && i != 10){
				increase += inc_inc;
				inc_inc *= 2;
			}
			t_max += increase;
		}
		//貢献度ptの上昇値
		t_max += playerdata.added_mana * SeichiAssist.config.getContributeAddedMana();

		setMax(t_max);
		return t_max;
	}



	//マナを最大値まで回復する処理
	public void fullMana(Player player,int level){
		this.m = this.getMax();
		displayMana(player,level);
	}
	public void setMana(double m) {
		this.m = m;
	}
	public double getMana() {
		return this.m;
	}
	public boolean isloaded() {
		return this.loadflag;
	}
	public double getMax() {
		return max;
	}
	public void setMax(double max) {
		this.max = max;
	}
}
