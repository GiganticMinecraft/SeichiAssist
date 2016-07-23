package com.github.unchama.multiseichieffect;

import com.github.unchama.multiseichieffect.Util.*;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class MinuteTaskRunnable extends BukkitRunnable{
	HashMap<Player,PlayerData> playermap = MultiSeichiEffect.playermap;
	
	//値の宣言
	private Player player;
	private PlayerData playerdata;





	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	MinuteTaskRunnable() {
	}


	@Override
	public void run() {
		for (Entry<Player, PlayerData> map : playermap.entrySet()){
			player = map.getKey();
			playerdata = map.getValue();
			//オンラインプレイヤーのみの処理
			if(playerdata.onlineflag){
				//エフェクト処理
				addEffect();
				//ガチャ券の処理
				presentGachaTicket();
				
			}
			//全プレイヤーへの処理
		}
	}




	private void addEffect() {
		Effect effect = new Effect(player);
		MineBlock mineblock = new MineBlock();
		
		//総破壊数を計算
		mineblock.last = calcMineBlock(player);
		effect.mineblock.setNow();
		effect.mineblock.setIncrease();

		//総破壊数によるeffectを計算
		effect.setMineblock();
		//ログイン人数によるeffectを計算
		effect.setPnum();

		//外部コマンド・プラグイン等による採掘速度上昇の検出
		effect.findOutEffect();
		//自作プラグイン内の上昇値計算
		effect.setMySum();
		//自作プラグインと外部プラグインの上昇値合算
		effect.setSum();

		//ポーション効果付与
		effect.addPotion(flag);



		//プレイヤーにメッセージ送信
		effect.sendEffectMessage();

		//lastの更新(最後にやろう)
		gacha.setLastPoint();
		effect.mineblock.setLast();
		effect.setLastSum();
		effect.setLastMySum();

		
	}
	private void presentGachaTicket() {
		//ガチャ用採掘量データセット
		gacha.setPoint(effect.mineblock.getIncrease());
		//ガチャ券付与
		gacha.presentticket();
	}

}


