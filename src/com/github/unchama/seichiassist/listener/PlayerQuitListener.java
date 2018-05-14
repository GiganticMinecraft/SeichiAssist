package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;

public class PlayerQuitListener implements Listener {
	SeichiAssist plugin = SeichiAssist.plugin;
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	Sql sql = SeichiAssist.sql;

	//プレイヤーがquitした時に実行
	@EventHandler(priority = EventPriority.HIGH)
	public void onplayerQuitEvent(PlayerQuitEvent event){
		//退出したplayerを取得
		Player player = event.getPlayer();
		//プレイヤーのuuidを取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ取得
		PlayerData playerdata = playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			Bukkit.getLogger().warning(player.getName() + " -> PlayerData not found.");
			Bukkit.getLogger().warning("PlayerQuitListener.onplayerQuitEvent");
			return;
		}
		//quit時とondisable時、プレイヤーデータを最新の状態に更新
		playerdata.updateonQuit(player);
		//タスクをすべて終了する
		playerdata.activeskilldata.RemoveAllTask();
		//saveplayerdata
		sql.saveQuitPlayerData(playerdata);

		//不要なplayerdataを削除
		playermap.remove(uuid);

	}

}
