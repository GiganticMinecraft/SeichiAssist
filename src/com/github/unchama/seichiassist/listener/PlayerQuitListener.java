package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;

public class PlayerQuitListener implements Listener {
	SeichiAssist plugin = SeichiAssist.plugin;
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	Sql sql = SeichiAssist.plugin.sql;

	//プレイヤーがquitした時に実行
	@EventHandler
	public void onplayerQuitEvent(PlayerQuitEvent event){
		//退出したplayerを取得
		Player player = event.getPlayer();
		//プレイヤーのuuidを取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ取得
		PlayerData playerdata = playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[Quit処理]でエラー発生");
			plugin.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return;
		}
		//quit時とondisable時、プレイヤーデータを最新の状態に更新
		playerdata.updateonQuit(player);
		//タスクをすべて終了する
		playerdata.activeskilldata.RemoveAllTask();
		//sqlコネクションチェック
		sql.checkConnection();
		//ログインフラグ折る(必ずsaveplayerdataの前に実行)
		if(!sql.logoutPlayerData(playerdata)){
			plugin.getLogger().warning(playerdata.name + "のloginflag->false化に失敗しました");
		}else{
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + player.getName() + "のloginflag回収完了");
		}
		sql.savePlayerData(playerdata);

		//不要なplayerdataを削除
		playermap.remove(uuid);

	}

}
