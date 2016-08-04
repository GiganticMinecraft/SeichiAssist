package com.github.unchama.seichiassist.task;


import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.EffectData;
import com.github.unchama.seichiassist.data.MineBlock;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class MinuteTaskRunnable extends BukkitRunnable{
	private SeichiAssist plugin = SeichiAssist.plugin;
	private HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap;
	private Config config = SeichiAssist.config;

	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	public MinuteTaskRunnable() {

	}

	@Override
	public void run() {
		playermap = SeichiAssist.playermap;
		plugin = SeichiAssist.plugin;
		if(SeichiAssist.DEBUG){
			Util.sendEveryMessage("プレイヤーの１分間の処理を実行");
		}

		//オンラインプレイヤーの人数を取得
		int onlinenums = plugin.getServer().getOnlinePlayers().size();


		//playermapが空の時return
		if(playermap.isEmpty()){
			return;
		}
		//プレイヤーマップに記録されているすべてのplayerdataについての処理
		for(PlayerData playerdata : playermap.values()){
			//プレイヤーのオンラインオフラインに関係なく処理
			//エフェクトデータの持続時間を1200tick引いて、０以下のものを削除
			playerdata.calcEffectData();

			//プレイヤーがオフラインの時処理を終了、次のプレイヤーへ
			if(playerdata.isOffline()){
				if(SeichiAssist.DEBUG){
					Util.sendEveryMessage(playerdata.name + "は不在により処理中止");
				}
				continue;
			}
			//プレイﾔｰが必ずオンラインと分かっている処理
			//プレイヤーを取得
			Player player = plugin.getServer().getPlayer(playerdata.uuid);
			//プレイヤー名を取得
			String name = Util.getName(player);
			int mines = MineBlock.calcMineBlock(player);
			//Levelを設定
			playerdata.levelupdata(player,mines);
			//詫び券の配布
			playerdata.giveSorryForBug(player);

			if(SeichiAssist.DEBUG){
				Util.sendEveryMessage(playerdata.name + "のランク処理完了");
			}
			//現在（after）の統計量を設定
			playerdata.minuteblock.after = mines;
			//1分前(before)との差を計算し、設定
			playerdata.minuteblock.setIncrease();
			//現在の統計量を設定(before)
			playerdata.minuteblock.before = mines;

			//effectの大きさ
			double amplifier = 0;
			//effectのメッセージ
			String string;
			//１分間のブロック破壊量による上昇
			amplifier = (double) playerdata.minuteblock.increase * config.getMinuteMineSpeed();
			string = "１分間のブロック破壊量(" + playerdata.minuteblock.increase + "個)からの上昇値:" + amplifier;
			playerdata.effectdatalist.add(new EffectData(amplifier,string));

			//プレイヤー数による上昇
			amplifier = (double) onlinenums * config.getLoginPlayerMineSpeed();
			string = "プレイヤー数(" + onlinenums + "人)からの上昇値:" + amplifier;
			playerdata.effectdatalist.add(new EffectData(amplifier,string));


			//effect追加の処理
			//合計effect量
			double sum = 0;
			//最大持続時間
			int maxduration = 0;
			//実際に適用されるeffect量
			int minespeedlv = 0;


			//effectflag=trueの時のみ実行
			if(playerdata.effectflag){
				//effectdatalistにある全てのeffectについて計算
				for(EffectData ed :playerdata.effectdatalist){
					//effect量を加算
					sum += ed.amplifier;
					//持続時間の最大値を取得
					if(maxduration < ed.duration){
						maxduration = ed.duration;
					}
				}
				//実際のeffect値をsum-1の切り捨て整数値に設定
				minespeedlv = (int)(sum - 1);

				//実際のeffect値が0より小さいときはeffectを適用しない
				if(minespeedlv < 0){
					player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0, false, false), true);
				}else{
					player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, maxduration, minespeedlv, false, false), true);
				}
				//プレイヤーデータを更新
				playerdata.minespeedlv = minespeedlv;
			}

			//プレイヤーにメッセージ送信
			if(playerdata.lastminespeedlv != minespeedlv || playerdata.messageflag){//前の上昇量と今の上昇量が違うか内訳表示フラグがオンの時告知する
				player.sendMessage(ChatColor.YELLOW + "★" + ChatColor.WHITE + "採掘速度上昇レベルが" + ChatColor.YELLOW + (minespeedlv+1) + ChatColor.WHITE +"になりました。");
				if(playerdata.messageflag){
					player.sendMessage("----------------------------内訳-----------------------------");
					for(EffectData ed : playerdata.effectdatalist){
						player.sendMessage(ed.string + "(持続時間:" + Util.toTimeString(ed.duration/20) + ")");
					}
					player.sendMessage("-------------------------------------------------------------");
				}
			}

			//プレイヤーデータを更新
			playerdata.lastminespeedlv = minespeedlv;

			if(SeichiAssist.DEBUG){
				Util.sendEveryMessage(playerdata.name + "のエフェクト処理が成功");
			}

			//ガチャ券付与の処理

			//ガチャポイントに合算
			playerdata.gachapoint += playerdata.minuteblock.increase;

			ItemStack skull = Util.getskull(name);
			if(playerdata.gachapoint >= config.getGachaPresentInterval()){
				playerdata.gachapoint -= config.getGachaPresentInterval();
				if(!player.getInventory().contains(skull) && Util.isPlayerInventryNoEmpty(player)){
					Util.dropItem(player,skull);
					player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
					player.sendMessage(ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "がドロップしました。右クリックで使えるゾ");
				}else{
					Util.addItem(player,skull);
					player.sendMessage(ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "プレゼントフォーユー");
				}
			}else{
				if(playerdata.minuteblock.increase != 0){
					player.sendMessage("あと" + ChatColor.AQUA + (config.getGachaPresentInterval()-(playerdata.gachapoint % config.getGachaPresentInterval())) + ChatColor.WHITE + "ブロック整地すると" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "獲得ダヨ");
				}
			}
			//プレイヤーデータを更新
			playerdata.lastgachapoint = playerdata.gachapoint;

			if(SeichiAssist.DEBUG){
				Util.sendEveryMessage(playerdata.name + "のガチャ処理が成功");
			}

		}

	}
}