package com.github.unchama.seichiassist.task;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.Level;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Util;
import com.github.unchama.seichiassist.data.EffectData;
import com.github.unchama.seichiassist.data.MineBlock;
import com.github.unchama.seichiassist.data.PlayerData;

public class MinuteTaskRunnable extends BukkitRunnable{
	private SeichiAssist plugin = SeichiAssist.plugin;
	private HashMap<String,PlayerData> playermap = SeichiAssist.playermap;


	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	public MinuteTaskRunnable() {
	}

	@Override
	public void run() {
		playermap = SeichiAssist.playermap;
		plugin = SeichiAssist.plugin;

		for (String name: playermap.keySet()){
			//playerdataを取得
			PlayerData playerdata = playermap.get(name);
			if(SeichiAssist.DEBUG){
				Util.sendEveryMessage(name + "の１分間の処理を実行");
			}
			//ここからエフェクト関係の処理
			List<EffectData> tmplist = new ArrayList<EffectData>();

			//エフェクトデータの持続時間を1200tick引いて、０以下のものを削除
			for(EffectData ed : playerdata.effectdatalist){
				ed.duration -= 1200;
				tmplist.add(ed);
			}
			for(EffectData ed : tmplist){
				if(ed.duration <= 60){
					playerdata.effectdatalist.remove(ed);
				}
			}

			if(plugin.getServer().getPlayer(name) == null){
				if(SeichiAssist.DEBUG){
					Util.sendEveryMessage(name + "は不在により処理中止");
				}
				continue;
			}

			//player型を再取得
			Player player = plugin.getServer().getPlayer(name);

			//Rankを設定
			Level.updata(player);


			if(SeichiAssist.DEBUG){
				Util.sendEveryMessage(name + "のランク処理完了");
			}

			//独自effect量計算
			//統計を抜き出し
			playerdata.minuteblock.after = MineBlock.calcMineBlock(player);

			//１分前の統計からの増減を取得
			playerdata.minuteblock.setIncrease();



			//現在の統計をbeforeに代入
			playerdata.minuteblock.before = playerdata.minuteblock.after;

			double amplifier = 0;
			String string;
			//１分間のブロック破壊量による上昇
			amplifier = (double) playerdata.minuteblock.increase * Config.getMinuteMineSpeed();
			string = "１分間のブロック破壊量(" + playerdata.minuteblock.increase + "個)からの上昇値:" + amplifier;
			playerdata.effectdatalist.add(new EffectData(amplifier,string));

			//プレイヤー数による上昇
			amplifier = (double) plugin.getServer().getOnlinePlayers().size() * Config.getLoginPlayerMineSpeed();
			string = "プレイヤー数(" + plugin.getServer().getOnlinePlayers().size() + "人)からの上昇値:" + amplifier;
			playerdata.effectdatalist.add(new EffectData(amplifier,string));


			//effect追加の処理
			double sum = 0;
			int maxduration = 0;
			int minespeedlv = 0;
			if(playerdata.effectflag){
				for(EffectData ed :playerdata.effectdatalist){
					sum += ed.amplifier;
					if(maxduration < ed.duration){
						maxduration = ed.duration;
					}
				}
				minespeedlv = (int)(sum - 1);
				if(minespeedlv < 0){
					player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0, false, false), true);
				}else{
					player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, maxduration, minespeedlv, false, false), true);
				}
				playerdata.minespeedlv = minespeedlv;
			}

			//プレイヤーにメッセージ送信
			if(playerdata.minespeedlv != minespeedlv || playerdata.messageflag){//前の上昇量と今の上昇量が違うか内訳表示フラグがオンの時告知する
				playerdata.minespeedlv = minespeedlv;
				player.sendMessage(ChatColor.YELLOW + "★" + ChatColor.WHITE + "採掘速度上昇レベルが" + ChatColor.YELLOW + (minespeedlv+1) + ChatColor.WHITE +"になりました。");
				if(playerdata.messageflag){
					player.sendMessage("----------------------------内訳-----------------------------");
					for(EffectData ed : playerdata.effectdatalist){
						player.sendMessage(ed.string + "(持続時間:" + Util.toTimeString(ed.duration/20) + ")");
					}
					player.sendMessage("-------------------------------------------------------------");
				}
			}
			if(SeichiAssist.DEBUG){
				Util.sendEveryMessage(name + "のエフェクト処理が成功");
			}
			//ガチャ券付与の処理

			//ガチャポイントに合算
			playerdata.gachapoint += playerdata.minuteblock.increase;

			ItemStack skull = Util.getskull();
			if(playerdata.gachapoint >= Config.getGachaPresentInterval()){
				playerdata.gachapoint -= Config.getGachaPresentInterval();
				if(!player.getInventory().contains(skull) && Util.isPlayerInventryEmpty(player)){
					Util.dropItem(player,skull);
					player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
					player.sendMessage(ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "が下に落ちました。右クリックで使えるゾ");
				}else{
					Util.addItem(player,skull);
					player.sendMessage(ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "プレゼントフォーユー");
				}
			}else{
				if(playerdata.gachapoint != playerdata.lastgachapoint){
					player.sendMessage("あと" + ChatColor.AQUA + (Config.getGachaPresentInterval()-(playerdata.gachapoint % Config.getGachaPresentInterval())) + ChatColor.WHITE + "ブロック整地すると" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "獲得ダヨ");
				}
			}
			playerdata.lastgachapoint = playerdata.gachapoint;
			if(SeichiAssist.DEBUG){
				Util.sendEveryMessage(name + "のガチャ処理が成功");
			}

		}
	}
}