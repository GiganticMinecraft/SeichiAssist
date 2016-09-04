package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class PlayerDataBackupTaskRunnable extends BukkitRunnable{
	SeichiAssist plugin = SeichiAssist.plugin;
	Sql sql = SeichiAssist.plugin.sql;
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;

	public PlayerDataBackupTaskRunnable(){

	}

	@Override
	public void run() {
		Util.sendEveryMessage(ChatColor.AQUA + "プレイヤーデータセーブ中…");
		plugin.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "プレイヤーデータセーブ中…");
		//現在オンラインのプレイヤーのプレイヤーデータを送信
		for(Player p : plugin.getServer().getOnlinePlayers()){
			//UUIDを取得
			UUID uuid = p.getUniqueId();
			PlayerData playerdata = playermap.get(uuid);
			if(!sql.savePlayerData(playerdata)){
				plugin.getLogger().info(ChatColor.RED + playerdata.name + "のデータ保存に失敗しました");
			}
		}
		//ランキングデータをセット
		sql.setRanking();
		Util.sendEveryMessage(ChatColor.AQUA + "プレイヤーデータセーブ完了");
		plugin.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "プレイヤーデータセーブ完了");
	}

}
