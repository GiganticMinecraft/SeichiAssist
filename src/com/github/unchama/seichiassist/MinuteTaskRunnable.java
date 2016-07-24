package com.github.unchama.seichiassist;


import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class MinuteTaskRunnable extends BukkitRunnable{
	SeichiAssist plugin;
	private HashMap<Player,PlayerData> playermap;
	PlayerData playerdata;
	double amplifier;
	String string;

	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	MinuteTaskRunnable() {
	}

	@Override
	public void run() {
		playermap = SeichiAssist.playermap;
		plugin = SeichiAssist.plugin;

		for (Player player : plugin.getServer().getOnlinePlayers()){
			//playerdataを取得
			playerdata = playermap.get(player);

			//独自効果をすべて削除
			playerdata.effectdatalist.clear();

			//独自effect量計算
			//統計を抜き出し
			playerdata.minuteblock.after = Util.calcMineBlock(player);

			//１分前の統計からの増減を取得
			playerdata.minuteblock.setIncrease();

			//ガチャポイントに合算
			playerdata.gachapoint += playerdata.minuteblock.increase;

			//現在の統計をbeforeに代入
			playerdata.minuteblock.before = playerdata.minuteblock.after;

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
			int amplifier = 0;
			if(playerdata.effectflag){
				for(EffectData ed :playerdata.effectdatalist){
					sum += ed.amplifier;
					if(maxduration < ed.duration){
						maxduration = ed.duration;
					}
				}
				amplifier = (int)(sum - 1);
				if(amplifier < 0){
					player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0, false, false), true);
				}else{
					player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, maxduration, amplifier, false, false), true);
				}
			}

			//プレイヤーにメッセージ送信
			if(playerdata.amplifier != amplifier){//前の上昇量と今の上昇量が違う場合告知する
				playerdata.amplifier = amplifier;
				player.sendMessage(ChatColor.YELLOW + "★" + ChatColor.WHITE + "採掘速度上昇レベルが" + ChatColor.YELLOW + (amplifier+1) + ChatColor.WHITE +"になりました。");
				if(playerdata.messageflag){
					player.sendMessage("----------内訳----------");
					for(EffectData ed : playerdata.effectdatalist){
						player.sendMessage(ed.string);
					}
					player.sendMessage("------------------------");
				}
			}

			//ガチャ券付与の処理
			ItemStack skull = Util.getskull();
			if(playerdata.gachapoint >= Config.getGachaPresentInterval()){
				playerdata.gachapoint -= Config.getGachaPresentInterval();
				if(!player.getInventory().contains(skull) && Util.isPlayerInventryEmpty(player)){
					Util.dropItem(player,skull);
					player.sendMessage("あなたの"+ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "地べたに置いたわよ忘れるんじゃないよ");
				}else{
					Util.addItem(player,skull);
					player.sendMessage(ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "プレゼントフォーユー");
				}
			}else{
				if(playerdata.gachapoint != playerdata.lastgachapoint){
					player.sendMessage("あと" + ChatColor.AQUA + (Config.getGachaPresentInterval() - playerdata.gachapoint) + ChatColor.WHITE + "ブロック整地すると" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "獲得ダヨ");
				}
			}
			playerdata.lastgachapoint = playerdata.gachapoint;
		}
	}
}