/*** Eclipse Class Decompiler plugin, copyright (c) 2012 Chao Chen (cnfree2000@hotmail.com) ***/
package com.github.unchama.buildassist;

import com.github.unchama.seichiassist.SeichiAssist;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

public class MinuteTaskRunnable extends BukkitRunnable {
	private Plugin plugin = BuildAssist.plugin;
	private HashMap<UUID, PlayerData> playermap = BuildAssist.playermap;


	public MinuteTaskRunnable() {
	}

	@Override
	public void run() {
		this.playermap = BuildAssist.playermap;
		this.plugin = BuildAssist.plugin;
		if (this.playermap.isEmpty()) {
			return;
		}
		for (PlayerData playerdata : this.playermap.values()) {
			if (!playerdata.isOffline()) {
				Player player = this.plugin.getServer().getPlayer(
						playerdata.uuid);
				//SeichiAssistのデータを取得
				UUID uuid = player.getUniqueId();
				com.github.unchama.seichiassist.data.player.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
				//経験値変更用のクラスを設定
				ExperienceManager expman = new ExperienceManager(player);

				int minus = -BuildAssist.config.getFlyExp();

				//1分間の建築量を加算する
//				player.sendMessage("1分の設置数:" + playerdata.build_num_1min);
//				player.sendMessage("累計設置数:" + playerdata.totalbuildnum);

				//player.sendMessage("建築量計算処理開始。1分の設置量:" + playerdata.build_num_1min.doubleValue() + ",累計設置量(before):" + playerdata.totalbuildnum.doubleValue());

				if(playerdata.build_num_1min.doubleValue() > BuildAssist.config.getBuildNum1minLimit()){
					playerdata.totalbuildnum = playerdata.totalbuildnum.add(new BigDecimal(BuildAssist.config.getBuildNum1minLimit()));
				}else{
					playerdata.totalbuildnum = playerdata.totalbuildnum.add(playerdata.build_num_1min);
				}
				playerdata.build_num_1min = BigDecimal.ZERO;

				//player.sendMessage("設置量計算処理終了。累計設置量(after):" + playerdata.totalbuildnum.doubleValue());

//				player.sendMessage("累計設置数:" + playerdata.totalbuildnum);

				playerdata.updateLevel(player);
				playerdata.buildsave(player);

				if (playerdata.Endlessfly) {
					if (playerdata_s.getIdleMinute() >= 10) {
						player.setAllowFlight(true);
						player.sendMessage(ChatColor.GRAY + "放置時間中のFLYは無期限で継続中です(経験値は消費しません)");
					} else if (!expman.hasExp(BuildAssist.config.getFlyExp())) {
						player.sendMessage(ChatColor.RED
								+ "Fly効果の発動に必要な経験値が不足しているため、");
						player.sendMessage(ChatColor.RED + "Fly効果を終了しました");
						playerdata.flytime = 0;
						playerdata.flyflag = false;
						playerdata.Endlessfly = false ;
						player.setAllowFlight(false);
						player.setFlying(false);
					} else {
						player.setAllowFlight(true);
						player.sendMessage(ChatColor.GREEN + "Fly効果は無期限で継続中です");
						expman.changeExp(minus);
					}
				}else if (playerdata.flyflag) {
					int flytime = playerdata.flytime;
					if (playerdata_s.getIdleMinute() >= 10) {
						player.setAllowFlight(true);
						player.sendMessage(ChatColor.GRAY + "放置時間中のFLYは無期限で継続中です(経験値は消費しません)");
					} else if (flytime <= 0) {
						player.sendMessage(ChatColor.GREEN + "Fly効果が終了しました");
						playerdata.flyflag = false;
						player.setAllowFlight(false);
						player.setFlying(false);
					} else if (!expman.hasExp(BuildAssist.config.getFlyExp())) {
						player.sendMessage(ChatColor.RED
								+ "Fly効果の発動に必要な経験値が不足しているため、");
						player.sendMessage(ChatColor.RED + "Fly効果を終了しました");
						playerdata.flytime = 0;
						playerdata.flyflag = false;
						player.setAllowFlight(false);
						player.setFlying(false);
					} else {
						player.setAllowFlight(true);
						player.sendMessage(ChatColor.GREEN + "Fly効果はあと"
								+ flytime + "分です");
						playerdata.flytime -= 1;
						expman.changeExp(minus);
					}
				}
			}
		}
	}
}
