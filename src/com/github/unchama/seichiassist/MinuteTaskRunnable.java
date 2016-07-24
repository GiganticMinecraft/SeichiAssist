package com.github.unchama.seichiassist;

import static com.github.unchama.seichiassist.Util.*;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MinuteTaskRunnable extends BukkitRunnable{
	HashMap<Player,PlayerData> playermap = SeichiAssist.playermap;
	SeichiAssist plugin;

	//値の宣言
	private Player player;
	private PlayerData playerdata;

	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	MinuteTaskRunnable(SeichiAssist _plugin) {
		plugin = _plugin;
	}


	@Override
	public void run() {
		for (Player p : plugin.getServer().getOnlinePlayers()){
			if(!playermap.containsKey(p)){
				playermap.put(p,new PlayerData(p));
			}
			player = p;
			playerdata = playermap.get(player);
			//現在のエフェクトデータ判別処理
			//getEffect();
			//エフェクト計算
			//calcEffect();
			//エンチャント付
			//addEffect();
			//ガチャ券の処理
			presentGachaTicket();
		}
	}
/*
	private void getEffect() {

		for(EffectData ed :playerdata.effectdatalist){
			ed.duration -= 60;
		}

		playerdata.minespeed -= 60;


		//現在のエフェクトリストを全て保存
		PotionEffect[] potioneffectlist = player.getActivePotionEffects().toArray(new PotionEffect[0]);
		for(PotionEffect pe : potioneffectlist){
			if(pe.getType().equals(PotionEffectType.FAST_DIGGING)){

					if(pe.getDuration()/20 + 2  >= playerdata.minespeed.amplifier && pe.getDuration()/20 - 2  <= playerdata.minespeed.amplifier ){
				}else{

				}
			}
		}
		実装失敗
		for(PotionEffect pe : potioneffectlist){
			if(pe.getDuration()/20>1){
				if(playerdata.effectdatalist.contains(pe)){
					for(EffectData ed :playerdata.effectdatalist){
						if(ed.potioneffecttype.equals(pe.getType())){
							if(pe.getDuration()/20 == ed.duration){
								//同じポーション効果だった時の振り分け
							}else{
								playerdata.effectdatalist.add(new EffectData(pe.getType(),pe.getDuration()/20,(double)pe.getAmplifier()));
							}
						}
					}
				}else{
					playerdata.effectdatalist.add(new EffectData(pe.getType(),pe.getDuration()/20,(double)pe.getAmplifier()));
				}
			}else{
				playerdata.effectdatalist.remove(pe);
			}
		}

	}
 */
/*
	private void calcEffect() {
		MineBlock mineblock = playerdata.minuteblock;

		//現在の総破壊数
		mineblock.after = Util.calcMineBlock(player);
		//前回の総破壊数との差
		mineblock.increase = mineblock.after - mineblock.before;
		//今回の破壊数を前回のものに設定
		mineblock.before = mineblock.after;
		//総破壊数によるeffectを計算
		playerdata.effectdatalist.add(new EffectData(PotionEffectType.FAST_DIGGING,
													 (double) mineblock.increase * getMinuteMineSpeed()
													));
		//ログイン人数によるeffectを計算
		playerdata.effectdatalist.add(new EffectData(PotionEffectType.FAST_DIGGING,
													 (double) getOnlinePlayer() * getLoginPlayerMineSpeed()
													));

	}
	private void addEffect() {
		//EffectDataにはDurationとamplifierだけ記憶させておく．
		HashMap<PotionEffectType,EffectData> ptypemap = new HashMap<PotionEffectType,EffectData>();
		//現在のエフェクトリストを全て保存
		PotionEffect[] potioneffectlist = player.getActivePotionEffects().toArray(new PotionEffect[0]);
		for(PotionEffect pe : potioneffectlist){
			ptypemap.put(pe.getType(),new EffectData(pe.getDuration()/20,pe.getAmplifier() + 1));
		}

		//EffectDataにはDurationとamplifierだけ記憶させておく．
		HashMap<PotionEffectType,EffectData> typemap = new HashMap<PotionEffectType,EffectData>();
		for(EffectData ed : playerdata.effectdatalist){
			//プレイヤーの持つ全てのエフェクトを合算
			if(!typemap.containsKey(ed.potioneffecttype)){
				//typemapにプレイヤーの持つポーションエフェクトを追加
				typemap.put(ed.potioneffecttype, new EffectData(ed.duration,(double)ed.amplifier));
			}else{
				//すでにtypemapにプレイヤーのポーションエフェクトタイプが記録されている時
				//typemapの該当タイプデータを参照
				//増幅値を合算
				typemap.get(ed.potioneffecttype).amplifier += ed.amplifier;
				//持続時間は一番大きいものを保持
				if(typemap.get(ed.potioneffecttype).duration > ed.duration){
					typemap.get(ed.potioneffecttype).duration = ed.duration;
				}
			}
		}
		//加算処理した全エフェクトをplayerに付与
		for(Map.Entry<PotionEffectType, EffectData> e : typemap.entrySet()){
			int allamplifier = (int)e.getValue().amplifier - 1;
			if(ptypemap.containsKey(e.getKey()) && ptypemap.get(e.getKey()).amplifier == e.getValue().amplifier){

			}
			player.addPotionEffect(new PotionEffect(e.getKey(), 20*e.getValue().duration,allamplifier, false, false), true);
			//採掘速度のみ変更されている場合通知
			if(e.getKey() == PotionEffectType.FAST_DIGGING && playerdata.allamplifier != allamplifier){
				player.sendMessage("採掘速度上昇レベルが(" + ChatColor.RED + allamplifier + ChatColor.WHITE + ")になりました。");
				playerdata.allamplifier = allamplifier;
			}
		}
	}
	*/
	private void presentGachaTicket() {
		MineBlock point = playerdata.gachapoint;
		int interval = Config.getGachaPresentInterval();
		if(point.after >= interval){
			point.after -= interval;
			dropItem(player,Util.getskull());
			player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
			player.sendMessage(ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "が下に落ちました。右クリックで使えるゾ");
		}else if(point.after != point.before){
			player.sendMessage("あと" + ChatColor.AQUA + (interval - point.after) + ChatColor.WHITE + "ブロック整地すると" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "獲得ダヨ");
		}
		point.before = point.after;
	}

}


