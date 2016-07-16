package com.github.unchama.multiseichieffect;

import static com.github.unchama.multiseichieffect.Util.*;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Effect {
	private Player player;
	private Config config;


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
	Effect(Player _player,Config _config) {
		//関数初期化
		player = _player;
		config = _config;
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
			//既に採掘効果上昇がかかっている場合
			//player.sendMessage("うえ-1");
			PotionEffect[] potioneffect = player.getActivePotionEffects().toArray(new PotionEffect[0]);
			for( PotionEffect pe : potioneffect){
				//採掘速度上昇のエフェクトのデータ抽出
				//player.sendMessage("うえ0");
				if(pe.getType().equals(PotionEffectType.FAST_DIGGING)){
					//上昇値
					effect_now_sum = pe.getAmplifier();
					//持続時間
					effect_now_dulation = pe.getDuration();
					//player.sendMessage("now_sumとnow_dulationの値取得官僚" + now_sum + "-" +now_dulation);
					break;
				}
			}
		}else{
			//採掘効果がない場合
			//player.sendMessage("した-1");
			effect_now_sum = -1;
			effect_out_sum = 0;
			effect_now_dulation = -1;
		}
	}

	public void setMineblock(){
		effect_mineblock =(double) mineblock.getIncrease() * config.getMinuteMineSpeed();
	}

	public void setPnum(){
		effect_p_num = (double) getOnlinePlayer() * config.getLoginPlayerMineSpeed();
	}

	public void setMySum(){
		effect_mysum = (int)(effect_p_num + effect_mineblock - 1);
	}

	public void setSum(){
		//自作プラグインと外部プラグインの上昇値合算
		if(effect_now_sum == -1 || (int)(effect_now_sum - last_effect_mysum) == (int)effect_out_sum){
			//自作プラグインの上昇値のみが反映される場合叉は外部プラグインの持続時間が残っている場合
			//player.sendMessage("うえ");
			effect_sum = effect_out_sum + effect_mysum;
		}else{
			//外部プラグインの上昇値を検出した場合
			//player.sendMessage("した");
			effect_sum = effect_mysum + effect_now_sum;
			this.setOutSum();
		}
		//上昇値の例外判定
			if(effect_sum < 0){
				effect_sum = 0;
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
	public void addPotion(){
		//外部プラグインの上昇値が存在するときの場合分け
			if(effect_now_dulation == -1){
				//外部プラグインの上昇値が存在しないとき
				//player.sendMessage("うえ２");
				player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 1200, effect_sum, false, false), true);
			}else{
				//外部プラグインの上昇値が存在するとき
				//player.sendMessage("した２");
				player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, effect_now_dulation, effect_sum, false, false), true);
			}
	}
	public void sendEffectMessage(){
		//effect_sumの値が変わってたらお知らせする
				if(last_effect_sum != effect_sum){
					player.sendMessage("---------------------------------");
					player.sendMessage("採掘速度上昇レベルが" + ChatColor.YELLOW +(effect_sum + 1) + ChatColor.WHITE +"になりました。");
					player.sendMessage("内訳:接続人数(" + getOnlinePlayer()+ "人)→上昇値(" + effect_p_num + ")");
					player.sendMessage("    1分間のブロック破壊数(" + mineblock.getIncrease() + "個)→上昇値(" + Decimal(effect_mineblock) + ")");
					if(effect_out_sum != 0){
						player.sendMessage("    外部からの上昇量(" + effect_out_sum + ")→上昇値(" + effect_out_sum + ")");
					}
					player.sendMessage("---------------------------------");
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
