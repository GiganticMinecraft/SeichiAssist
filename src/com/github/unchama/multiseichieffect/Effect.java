package com.github.unchama.multiseichieffect;

import static com.github.unchama.multiseichieffect.Util.*;
import static com.github.unchama.multiseichieffect.Config.*;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Effect {
	private Player player;


	private boolean firstloginflag;
	private double effect_p_num;
	private double effect_mineblock;
	private int effect_sum;
	private int last_effect_sum;
	private int effect_now_sum;
	private int effect_out_sum;
	private int effect_mysum;
	private int last_effect_mysum;
	private int effect_now_dulation;
	public MineBlock mineblock;

	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	Effect(Player _player) {
		//関数初期化
		player = _player;
		firstloginflag = true;
		effect_p_num = 0.0;
		effect_mineblock = 0.0;
		effect_sum = 0;
		last_effect_sum = -1;
		effect_now_sum = -1;
		effect_out_sum = 0;
		effect_mysum = 0;
		last_effect_mysum = 0;
		effect_now_dulation = -1;
		mineblock = new MineBlock(player);
	}
	public void findOutEffect(){
		//外部コマンド・プラグイン等による採掘速度上昇の検出
		if(player.hasPotionEffect(PotionEffectType.FAST_DIGGING)){
			if(firstloginflag){
				//ログイン時に持っているバフは削除しちゃう(リログによるバフレベル増殖を防ぐ為)
				effect_now_sum = -1;
				effect_out_sum = 0;
				effect_now_dulation = -1;
				firstloginflag = false;
			}else{
				//既に採掘効果上昇がかかっている場合
				PotionEffect[] potioneffect = player.getActivePotionEffects().toArray(new PotionEffect[0]);
				for(PotionEffect pe : potioneffect){
					//採掘速度上昇のエフェクトのデータ抽出
					if(pe.getType().equals(PotionEffectType.FAST_DIGGING)){
						//上昇値
						//effect_now_sumは"内部上のレベル"の数値の為、ここで"表示上のレベル"の数値に変換(+1)してから読み込む
						effect_now_sum = pe.getAmplifier() + 1;
						//持続時間
						effect_now_dulation = pe.getDuration();
						break;
					}
				}
			}
		}else{
			//採掘効果がない場合、変数を初期化しておく
			effect_now_sum = -1;
			effect_out_sum = 0;
			effect_now_dulation = -1;
			firstloginflag = false;


		}
	}

	public void setMineblock(){
		effect_mineblock =(double) mineblock.getIncrease() * getMinuteMineSpeed();
	}

	public void setPnum(){
		effect_p_num = (double) getOnlinePlayer() * getLoginPlayerMineSpeed();
	}

	public void setMySum(){
		effect_mysum = (int)(effect_p_num + effect_mineblock);
	}

	public void setSum(){
		//自作プラグインと外部プラグインの上昇値合算
		//注意:effect_sumの計算は"表示上のレベル"の数値で行っている
		if(effect_now_sum == -1 || (int)(effect_now_sum - last_effect_mysum) == (int)effect_out_sum){
			//自作プラグインの上昇値のみが反映される場合叉は外部プラグインの持続時間が残っている場合
			effect_sum = effect_mysum + effect_out_sum;
		}else{
			//外部プラグインの上昇値を検出した場合
			effect_sum = effect_mysum + effect_now_sum;
			this.setOutSum();
		}
		//上昇値の例外判定(もちろんここも"表示上のレベル"の数値で判定)
			if(effect_sum < 1){
				effect_sum = 1;
			}
	}
	public void setOutSum(){
		effect_out_sum = effect_now_sum;
	}
	public void setLastSum(){
		last_effect_sum = effect_sum;
	}
	public void setLastMySum(){
		last_effect_mysum = effect_mysum;
	}
	public void addPotion(Boolean flag){
		//外部プラグインの上昇値が存在するときの場合分け
		//effect_sumは"表示上のレベル"の数値の為、ここで内部数値に変換(-1)してから反映させる
		if(!flag){
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0, false, false), true);
		}else if(effect_now_dulation == -1){
			//外部プラグインの上昇値が存在しないとき
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 1200, effect_sum - 1, false, false), true);
		}else{
			//外部プラグインの上昇値が存在するとき
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, effect_now_dulation, effect_sum - 1, false, false), true);
		}
	}
	public void sendEffectMessage(){
		//effect_sumの値が変わってたらお知らせする
		if(last_effect_sum != effect_sum){
			if(effect_out_sum != 0){
				player.sendMessage(ChatColor.RED+"ドラゲナイタイム中!レベルボーナス(" + ChatColor.YELLOW + "+" + effect_out_sum + ChatColor.RED + ")");
				//player.sendMessage("※投票またはドラゲナイタイムによる上昇値(" + ChatColor.YELLOW + effect_out_sum + ChatColor.WHITE + ")残り時間(" + ChatColor.YELLOW + effect_now_dulation / 20 + ChatColor.WHITE + "秒)");
			}
			//player.sendMessage("---------------------------------");
			player.sendMessage("採掘速度上昇レベルが(" + ChatColor.RED + effect_sum + ChatColor.WHITE + ")になりました。※接続人数(" + ChatColor.YELLOW + effect_p_num + ChatColor.WHITE + ")+整地量(" + ChatColor.YELLOW + Decimal(effect_mineblock) + ChatColor.WHITE + ")");
			//player.sendMessage("※接続人数(" + ChatColor.YELLOW + getOnlinePlayer() + ChatColor.WHITE + "人)による上昇値(" + ChatColor.YELLOW + effect_p_num + ChatColor.WHITE + ")");
			//player.sendMessage("※1分間のブロック破壊数(" + ChatColor.YELLOW + mineblock.getIncrease() + ChatColor.WHITE + "個)による上昇値(" + ChatColor.YELLOW + Decimal(effect_mineblock) + ChatColor.WHITE + ")");
			//player.sendMessage("累積整地ブロック数は("+ ChatColor.RED + calcMineblock(player) + ChatColor.WHITE + "個)です。");
			//player.sendMessage("---------------------------------");
		}
	}
	public Player getPlayer(){
		return player;
	}
	public double getPnum(){
		return effect_p_num;
	}
	public double getMineBlock(){
		return effect_mineblock;
	}
	public int getSum(){
		return effect_sum;
	}
	public int getLastSum(){
		return last_effect_sum;
	}
	public int getNowSum(){
		return effect_now_sum;
	}
	public int getOutSum(){
		return effect_out_sum;
	}
	public int getMySum(){
		return effect_mysum;
	}
	public int getLastMySum(){
		return last_effect_mysum;
	}
	public int getNowDulation(){
		return effect_now_dulation;
	}
}
