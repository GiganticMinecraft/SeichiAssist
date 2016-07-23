package com.github.unchama.seichiassist;

import static com.github.unchama.seichiassist.Config.getLoginPlayerMineSpeed;
import static com.github.unchama.seichiassist.Config.getMinuteMineSpeed;
import static com.github.unchama.seichiassist.Util.getOnlinePlayer;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class MinuteTaskRunnable extends BukkitRunnable{
	HashMap<Player,PlayerData> playermap = SeichiAssist.playermap;
	
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
				//現在のエフェクトデータ判別処理
				getEffect();
				//エフェクト計算
				calcEffect();
				//エンチャント付
				addEffect();
				//ガチャ券の処理
				presentGachaTicket();
				
			}
			//全プレイヤーへの処理
		}
	}







	private void getEffect() {
		PotionEffect[] potioneffectlist = player.getActivePotionEffects().toArray(new PotionEffect[0]);
		for(PotionEffect pe : potioneffectlist){
			if(pe.getDuration()>1){
				playerdata.effectdatalist.add(new EffectData(pe.getType(),pe.getDuration(),(double)pe.getAmplifier()));
			}
		}
	}


	private void calcEffect() {
		PotionEffect effect;
		MineBlock mineblock = playerdata.minuteblock;
		
		//現在の総破壊数
		mineblock.last = Util.calcMineBlock(player);
		//前回の総破壊数との差
		mineblock.increase = mineblock.last - mineblock.now; 
		//総破壊数によるeffectを計算
		playerdata.effectdatalist.add(new EffectData(PotionEffectType.FAST_DIGGING,60,(double) mineblock.increase * getMinuteMineSpeed()));
		//ログイン人数によるeffectを計算
		playerdata.effectdatalist.add(new EffectData(PotionEffectType.FAST_DIGGING,60,(double) getOnlinePlayer() * getLoginPlayerMineSpeed()));



		//プレイヤーにメッセージ送信
		effect.sendEffectMessage();

		//lastの更新(最後にやろう)
		gacha.setLastPoint();
		effect.mineblock.setLast();
		effect.setLastSum();
		effect.setLastMySum();

		
	}
	private void addEffect() {

		HashMap<PotionEffectType,EffectData> typemap = new HashMap<PotionEffectType,EffectData>();
		
		for(EffectData ed : playerdata.effectdatalist){
			if(typemap.containsKey(ed.potioneffecttype)){
				EffectData mapd = typemap.get(ed.potioneffecttype);
				mapd.amplifier += ed.amplifier;
			}
		}
		player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 1200, sum - 1, false, false), true);
		
	}
	private void presentGachaTicket() {
		//ガチャ用採掘量データセット
		gacha.setPoint(effect.mineblock.getIncrease());
		//ガチャ券付与
		gacha.presentticket();
	}

}


