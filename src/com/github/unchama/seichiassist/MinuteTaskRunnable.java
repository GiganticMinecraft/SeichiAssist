package com.github.unchama.seichiassist;

import static com.github.unchama.seichiassist.Config.getLoginPlayerMineSpeed;
import static com.github.unchama.seichiassist.Config.getMinuteMineSpeed;
import static com.github.unchama.seichiassist.Util.dropItem;
import static com.github.unchama.seichiassist.Util.getOnlinePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
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
		//現在のエフェクトリストを全て保存
		PotionEffect[] potioneffectlist = player.getActivePotionEffects().toArray(new PotionEffect[0]);
		for(PotionEffect pe : potioneffectlist){
			if(pe.getDuration()>1){
				playerdata.effectdatalist.add(new EffectData(pe.getType(),pe.getDuration(),(double)pe.getAmplifier()));
			}
		}
	}


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
		HashMap<PotionEffectType,EffectData> typemap = new HashMap<PotionEffectType,EffectData>();
		
		for(EffectData ed : playerdata.effectdatalist){
			//プレイヤーの持つ全てのエフェクトを合算
			if(typemap.containsKey(ed.potioneffecttype)){
				//すでにtypemapにプレイヤーのポーションエフェクトタイプが記録されている時
				//typemapの該当タイプデータを参照
				EffectData alled = typemap.get(ed.potioneffecttype);
				//増幅値を合算
				alled.amplifier += ed.amplifier;
				//持続時間は一番大きいものを保持
				if(alled.duration > ed.duration){
					alled.duration = ed.duration;
				}
			}else{
				//typemapにプレイヤーの持つポーションエフェクトを追加
				typemap.put(ed.potioneffecttype, new EffectData(ed.duration,(double)ed.amplifier));
			}
		}
		//加算処理した全エフェクトをplayerに付与
		for(Map.Entry<PotionEffectType, EffectData> e : typemap.entrySet()){
			int allamplifier = (int)e.getValue().amplifier - 1;
			player.addPotionEffect(new PotionEffect(e.getKey(), 20*e.getValue().duration,allamplifier, false, false), true);
			//採掘速度のみ変更されている場合通知
			if(e.getKey() == PotionEffectType.FAST_DIGGING && playerdata.allamplifier == allamplifier){
				player.sendMessage("採掘速度上昇レベルが(" + ChatColor.RED + allamplifier + ChatColor.WHITE + ")になりました。");
				playerdata.allamplifier = allamplifier;
			}
		}
	}
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


