package com.github.unchama.seichiassist.data;



import net.md_5.bungee.api.ChatColor;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.util.Util;



public class Mana {
	//マナの値
	double m;
	//マックスの値
	double max;
	//バークラス
	BossBar manabar;
	//読み込まれているかどうかのフラグ
	boolean loadflag;

	//引数なしのコンストラクタ
	public Mana() {
		this.m = 0;
		this.max = 0;
		this.loadflag = false;
	}
	//必ず最初のみ実行してほしいメソッド
	public void update(Player player, int level) {
		//現在のレベルでのマナ上限値を計算しバーに表示
		//mの値は既に得られているはず。
		this.loadflag = true;
		this.calcMaxMana(level);
		displayMana(player,level);

	}
	//現在マナをマナバーに表示させる
	private void displayMana(Player player,int level){
		if(!loadflag)return;
		removeBar();
		setManaBar(player,level);

	}
	private void setManaBar(Player player,int level) {
		manabar = player.getServer().createBossBar(ChatColor.AQUA + "" + ChatColor.BOLD + "マナ(" + Util.Decimal(m) + "/" + max + ")", BarColor.BLUE, BarStyle.SOLID);

		if(m/max < 0.0 || m/max > 1.0){
			reset(level);
			player.sendMessage(ChatColor.RED + "不正な値がマナとして保存されていたためリセットしました。");
		}
		manabar.setProgress(m/max);
		manabar.addPlayer(player);
	}
	private void reset(int level) {
		this.calcMaxMana(level);
		if(this.m < 0.0)this.m = 0;
		if(this.m > max)this.m = max;
	}
	//現在のバーを削除する（更新するときは不要）
	private void removeBar(){
		try{manabar.removeAll();}catch(NullPointerException e){}
	}
	public void increaseMana(double i,Player player,int level){
		this.m += i;
		if(m > max) m = max;
		displayMana(player,level);
	}
	public void decreaseMana(double d,Player player,int level){
		this.m -= d;
		if(m < 0) m = 0;
		displayMana(player,level);
	}
	public boolean hasMana(double h){
		if(compare(m,h) < 0)return false;
		return true;
	}

	private int compare(double x, double y) {
		if(x > y)return 1;
		if(x == y)return 0;
		return -1;
	}
	//レベルアップするときに実行したい関数
	public void LevelUp(Player player,int level){
		calcMaxMana(level);
		fullMana();
		displayMana(player,level);
	}
	//マナ最大値を計算する処理
	public void calcMaxMana(int level){
		double t_max = 1;
		//レベルが10行っていない時1で処理を終了
		if(level < 10){
			this.max = t_max;
			return;
		}
		//１０行ってる時の処理
		t_max = 100;
		int increase = 10;
		int inc_inc = 2;
		//１１以降の処理
		for(int i = 10 ; i < level;i++){
			if(i%10 == 0 && i<110 && i != 10){
				increase += inc_inc;
				inc_inc *= 2;
			}
			t_max += increase;
		}
		this.max = t_max;
		return;
	}
	//マナを最大値まで回復する処理
	public void fullMana(){
		this.m = this.max;
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

}
