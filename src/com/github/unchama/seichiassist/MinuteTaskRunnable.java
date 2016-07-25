package com.github.unchama.seichiassist;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class MinuteTaskRunnable extends BukkitRunnable{
	SeichiAssist plugin;
	private HashMap<String,PlayerData> playermap;
	Player player;
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
		List<EffectData> tmplist = new ArrayList<EffectData>();
		for (String name: playermap.keySet()){
			//playerdataを取得
			playerdata = playermap.get(name);

			//player型を再取得
			playerdata.player = plugin.getServer().getPlayer(name);
			player = playerdata.player;

			//エフェクトデータの持続時間を1200tick引いて、０以下のものを削除
			for(EffectData ed : playerdata.effectdatalist){
				ed.duration -= 1200;
				tmplist.add(ed);
			}
			for(EffectData ed : tmplist){
				if(ed.duration <= 0){
					playerdata.effectdatalist.remove(ed);
				}
			}

			if(!player.isOnline()){
				return;
			}

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
			if(playerdata.amplifier != amplifier || playerdata.messageflag){//前の上昇量と今の上昇量が違うか内訳表示フラグがオンの時告知する
				playerdata.amplifier = amplifier;
				player.sendMessage(ChatColor.YELLOW + "★" + ChatColor.WHITE + "採掘速度上昇レベルが" + ChatColor.YELLOW + (amplifier+1) + ChatColor.WHITE +"になりました。");
				if(playerdata.messageflag){
					player.sendMessage("----------------------------内訳-----------------------------");
					for(EffectData ed : playerdata.effectdatalist){
						player.sendMessage(ed.string + "(持続時間:" + ed.duration/20 + "秒)");
					}
					player.sendMessage("-------------------------------------------------------------");
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
	/*
	private void calcRankBorder(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Config config = plugin.getconfig();
		Integer level = 0;
		String name = player.getDisplayName();

		//係数有りパラメータ
		level += (int)((player).getStatistic(Statistic.MINE_BLOCK, Material.STONE) *
				config.getResultset().get("stone"));
		level += (int)((player).getStatistic(Statistic.MINE_BLOCK, Material.NETHERRACK) *
				config.getResultset().get("netherrack"));
		level +=(int)((player).getStatistic(Statistic.MINE_BLOCK, Material.DIRT) *
				config.getResultset().get("dirt"));
		level += (int)((player).getStatistic(Statistic.MINE_BLOCK, Material.GRAVEL) *
				config.getResultset().get("gravel"));
		level += (int)(((player).getStatistic(Statistic.MINE_BLOCK, Material.LOG) +
				(player).getStatistic(Statistic.MINE_BLOCK, Material.LOG_2)) *
				config.getResultset().get("log"));
		level += (int)((player).getStatistic(Statistic.USE_ITEM, Material.SEEDS) *
				config.getResultset().get("seed"));
		level += (int)((player).getStatistic(Statistic.USE_ITEM, Material.POTATO_ITEM) *
				config.getResultset().get("potate"));
		level += (int)((player).getStatistic(Statistic.USE_ITEM, Material.CARROT_ITEM) *
				config.getResultset().get("carrot"));

		//別途1倍パラメータ
		level += (int)((player).getStatistic(Statistic.MINE_BLOCK, Material.ENDER_STONE));

		//比較対象はツリーマップなので、最小値から順に比べ、整地力に応じて設定する
		for(Integer i : config.getRankset().keySet()){
			if(level >= i){
				//プレイヤーネームの設定
				name = config.getRankset().get(i).getName(player);
				//権限の付与
				config.getRankset().get(i).setPermissions(player.addAttachment(plugin), player);
			}else{
				break;
			}
		}

		player.setDisplayName(name);
	}*/
}