package com.github.unchama.seichiassist;

import static com.github.unchama.seichiassist.Config.*;
import static com.github.unchama.seichiassist.Util.*;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Effect {

	double p_num;
	double mineblock;
	int sum;
	int last_sum;
	int now_sum;
	int out_sum;
	int mysum;
	int last_mysum;
	int now_dulation;

	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	Effect() {
		//関数初期化
		p_num = 0.0;
		mineblock = 0.0;
		sum = 0;
		last_sum = -1;
		now_sum = -1;
		out_sum = 0;
		mysum = 0;
		last_mysum = 0;
		now_dulation = -1;
	}
	public void findOutEffect(){
		//外部コマンド・プラグイン等による採掘速度上昇の検出
		if(player.hasPotionEffect(PotionEffectType.FAST_DIGGING)){
			if(firstloginflag){
				//ログイン時に持っているバフは削除しちゃう(リログによるバフレベル増殖を防ぐ為)
				now_sum = -1;
				out_sum = 0;
				now_dulation = -1;
				firstloginflag = false;
			}else{
				//既に採掘効果上昇がかかっている場合
				PotionEffect[] potioneffect = player.getActivePotionEffects().toArray(new PotionEffect[0]);
				for(PotionEffect pe : potioneffect){
					//採掘速度上昇のエフェクトのデータ抽出
					if(pe.getType().equals(PotionEffectType.FAST_DIGGING)){
						//上昇値
						//now_sumは"内部上のレベル"の数値の為、ここで"表示上のレベル"の数値に変換(+1)してから読み込む
						now_sum = pe.getAmplifier() + 1;
						//持続時間
						now_dulation = pe.getDuration();
						break;
					}
				}
			}
		}else{
			//採掘効果がない場合、変数を初期化しておく
			now_sum = -1;
			out_sum = 0;
			now_dulation = -1;
			firstloginflag = false;


		}
	}

	public void setMineblock(int _mineblock){
		mineblock = _mineblock;
	}

	public void setPnum(){
		p_num = (double) getOnlinePlayer() * getLoginPlayerMineSpeed();
	}

	public void setMySum(){
		mysum = (int)(p_num + mineblock);
	}

	public void setSum(){
		//自作プラグインと外部プラグインの上昇値合算
		//注意:sumの計算は"表示上のレベル"の数値で行っている
		if(now_sum == -1 || (int)(now_sum - last_mysum) == (int)out_sum){
			//自作プラグインの上昇値のみが反映される場合叉は外部プラグインの持続時間が残っている場合
			sum = mysum + out_sum;
		}else{
			//外部プラグインの上昇値を検出した場合
			sum = mysum + now_sum;
			this.setOutSum();
		}
		//上昇値の例外判定(もちろんここも"表示上のレベル"の数値で判定)
			if(sum < 1){
				sum = 1;
			}
	}
	public void setOutSum(){
		out_sum = now_sum;
	}
	public void setLastSum(){
		last_sum = sum;
	}
	public void setLastMySum(){
		last_mysum = mysum;
	}
	public void addPotion(Boolean flag){
		//外部プラグインの上昇値が存在するときの場合分け
		//sumは"表示上のレベル"の数値の為、ここで内部数値に変換(-1)してから反映させる
		if(!flag){
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0, false, false), true);
		}else if(now_dulation == -1){
			//外部プラグインの上昇値が存在しないとき
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 1200, sum - 1, false, false), true);
		}else{
			//外部プラグインの上昇値が存在するとき
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, now_dulation, sum - 1, false, false), true);
		}
	}
	public void sendEffectMessage(){
		//sumの値が変わってたらお知らせする
		if(last_sum != sum){
			if(out_sum != 0){
				player.sendMessage(ChatColor.RED+"ドラゲナイタイム中!レベルボーナス(" + ChatColor.YELLOW + "+" + out_sum + ChatColor.RED + ")");
				//player.sendMessage("※投票またはドラゲナイタイムによる上昇値(" + ChatColor.YELLOW + out_sum + ChatColor.WHITE + ")残り時間(" + ChatColor.YELLOW + now_dulation / 20 + ChatColor.WHITE + "秒)");
			}
			//player.sendMessage("---------------------------------");
			player.sendMessage("採掘速度上昇レベルが(" + ChatColor.RED + sum + ChatColor.WHITE + ")になりました。※接続人数(" + ChatColor.YELLOW + p_num + ChatColor.WHITE + ")+整地量(" + ChatColor.YELLOW + Decimal(mineblock) + ChatColor.WHITE + ")");
			//player.sendMessage("※接続人数(" + ChatColor.YELLOW + getOnlinePlayer() + ChatColor.WHITE + "人)による上昇値(" + ChatColor.YELLOW + p_num + ChatColor.WHITE + ")");
			//player.sendMessage("※1分間のブロック破壊数(" + ChatColor.YELLOW + mineblock.getIncrease() + ChatColor.WHITE + "個)による上昇値(" + ChatColor.YELLOW + Decimal(mineblock) + ChatColor.WHITE + ")");
			//player.sendMessage("累積整地ブロック数は("+ ChatColor.RED + calcMineblock(player) + ChatColor.WHITE + "個)です。");
			//player.sendMessage("---------------------------------");
		}
	}
	public Player getPlayer(){
		return player;
	}
	public double getPnum(){
		return p_num;
	}
	public double getMineBlock(){
		return mineblock;
	}
	public int getSum(){
		return sum;
	}
	public int getLastSum(){
		return last_sum;
	}
	public int getNowSum(){
		return now_sum;
	}
	public int getOutSum(){
		return out_sum;
	}
	public int getMySum(){
		return mysum;
	}
	public int getLastMySum(){
		return last_mysum;
	}
	public int getNowDulation(){
		return now_dulation;
	}
}
