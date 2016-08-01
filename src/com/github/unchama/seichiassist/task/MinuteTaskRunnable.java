package com.github.unchama.seichiassist.task;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.Util;
import com.github.unchama.seichiassist.data.EffectData;
import com.github.unchama.seichiassist.data.MineBlock;
import com.github.unchama.seichiassist.data.PlayerData;

public class MinuteTaskRunnable extends BukkitRunnable{
	private SeichiAssist plugin = SeichiAssist.plugin;
	private HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap;
	private Config config = SeichiAssist.config;
	private Sql sql = SeichiAssist.plugin.sql;
	private List<String> namelist;

	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	public MinuteTaskRunnable() {

	}

	@Override
	public void run() {
		playermap = SeichiAssist.playermap;
		plugin = SeichiAssist.plugin;
		namelist = sql.getNameList(SeichiAssist.PLAYERDATA_TABLENAME);
		if(SeichiAssist.DEBUG){
			Util.sendEveryMessage("プレイヤーの１分間の処理を実行");
		}
		if(playermap.isEmpty()){
			return;
		}
		for(String name : namelist){
			Player player = plugin.getServer().getPlayer(name);
			//プレイヤーのオンラインオフラインに関係なく処理
			UUID uuid = UUID.fromString(sql.selectstring(SeichiAssist.PLAYERDATA_TABLENAME,name, "uuid"));
			if(!playermap.containsKey(uuid)){
				Util.sendEveryMessage(name + "はサーバーリロード後、まだ一度も入っていないので処理中止");
				continue;
			}
			PlayerData playerdata = playermap.get(uuid);
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
			//プレイヤーがオフラインの時処理を終了、次のプレイヤーへ
			if(plugin.getServer().getPlayer(name) == null){
				if(SeichiAssist.DEBUG){
					Util.sendEveryMessage(name + "は不在により処理中止");
				}
				continue;
			}
			//プレイﾔｰが必ずオンラインと分かっている処理

			//Rankを設定
			Level.updata(player);
			//詫び券の配布
			playerdata.giveSorryForBug();


			if(SeichiAssist.DEBUG){
				Util.sendEveryMessage(name + "のランク処理完了");
			}
			int increase = 0;

			int after = MineBlock.calcMineBlock(player);
			sql.insert(SeichiAssist.PLAYERDATA_TABLENAME,"minuteafter",after, name);
			increase = sql.selectint(SeichiAssist.PLAYERDATA_TABLENAME,name, "minuteafter")-sql.selectint(SeichiAssist.PLAYERDATA_TABLENAME,name, "minutebefore");
			sql.insert(SeichiAssist.PLAYERDATA_TABLENAME,"minuteincrease",increase, name);
			sql.insert(SeichiAssist.PLAYERDATA_TABLENAME,"minutebefore",after, name);


			double amplifier = 0;
			String string;
			//１分間のブロック破壊量による上昇
			amplifier = (double) increase * config.getMinuteMineSpeed();
			string = "１分間のブロック破壊量(" + increase + "個)からの上昇値:" + amplifier;
			playerdata.effectdatalist.add(new EffectData(amplifier,string));

			//プレイヤー数による上昇
			amplifier = (double) plugin.getServer().getOnlinePlayers().size() * config.getLoginPlayerMineSpeed();
			string = "プレイヤー数(" + plugin.getServer().getOnlinePlayers().size() + "人)からの上昇値:" + amplifier;
			playerdata.effectdatalist.add(new EffectData(amplifier,string));


			//effect追加の処理
			double sum = 0;
			int maxduration = 0;
			int minespeedlv = 0;
			if(sql.selectboolean(SeichiAssist.PLAYERDATA_TABLENAME,name, "effectflag")){
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
			}
			sql.insert(SeichiAssist.PLAYERDATA_TABLENAME,"minespeedlv", minespeedlv, name);
			//プレイヤーにメッセージ送信
			if(sql.selectint(SeichiAssist.PLAYERDATA_TABLENAME,name, "lastminespeedlv") != minespeedlv || sql.selectboolean(SeichiAssist.PLAYERDATA_TABLENAME,name, "messageflag")){//前の上昇量と今の上昇量が違うか内訳表示フラグがオンの時告知する
				player.sendMessage(ChatColor.YELLOW + "★" + ChatColor.WHITE + "採掘速度上昇レベルが" + ChatColor.YELLOW + (minespeedlv+1) + ChatColor.WHITE +"になりました。");
				if(sql.selectboolean(SeichiAssist.PLAYERDATA_TABLENAME,name, "messageflag")){
					player.sendMessage("----------------------------内訳-----------------------------");
					for(EffectData ed : playerdata.effectdatalist){
						player.sendMessage(ed.string + "(持続時間:" + Util.toTimeString(ed.duration/20) + ")");
					}
					player.sendMessage("-------------------------------------------------------------");
				}
			}
			sql.insert(SeichiAssist.PLAYERDATA_TABLENAME,"lastminespeedlv", minespeedlv, name);
			if(SeichiAssist.DEBUG){
				Util.sendEveryMessage(name + "のエフェクト処理が成功");
			}
			//ガチャ券付与の処理

			//ガチャポイントに合算
			int gachapoint = sql.selectint(SeichiAssist.PLAYERDATA_TABLENAME,name, "gachapoint") + increase;

			ItemStack skull = Util.getskull();
			if(gachapoint >= config.getGachaPresentInterval()){
				gachapoint -= config.getGachaPresentInterval();
				if(!player.getInventory().contains(skull) && Util.isPlayerInventryNoEmpty(player)){
					Util.dropItem(player,skull);
					player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
					player.sendMessage(ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "がドロップしました。右クリックで使えるゾ");
				}else{
					Util.addItem(player,skull);
					player.sendMessage(ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "プレゼントフォーユー");
				}
			}else{
				if(increase != 0){
					player.sendMessage("あと" + ChatColor.AQUA + (config.getGachaPresentInterval()-(gachapoint % config.getGachaPresentInterval())) + ChatColor.WHITE + "ブロック整地すると" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "獲得ダヨ");
				}
			}
			sql.insert(SeichiAssist.PLAYERDATA_TABLENAME,"gachapoint", gachapoint, name);
			if(SeichiAssist.DEBUG){
				Util.sendEveryMessage(name + "のガチャ処理が成功");
			}

		}

	}
}