package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;

public class PlayerJoinListener implements Listener {
	private SeichiAssist plugin = SeichiAssist.plugin;
	private HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	private Sql sql = SeichiAssist.plugin.sql;


	/*
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		if ((e.getResult().equals(PlayerLoginEvent.Result.KICK_FULL))
				&& (e.getPlayer().hasPermission("SeichiAssist.fulljoin"))) {
			e.allow();
		}
	}
	*/

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		if ((e.getResult().equals(PlayerLoginEvent.Result.KICK_FULL))) {
			if(e.getPlayer().hasPermission("SeichiAssist.fullstay")){
				e.allow();
				return;
			}
			for(Player p : plugin.getServer().getOnlinePlayers()){
				if(p.hasPermission("SeichiAssist.fullstay")){
					continue;
				}
				//UUIDを取得
				UUID uuid = p.getUniqueId();
				PlayerData playerdata = playermap.get(uuid);
				if(playerdata.idletime >= 10){
					p.kickPlayer("放置プレイヤーはキックされます(満員時のみ)。再度ログインすることが可能です。");
					e.allow();
					break;
				}
			}
		}
	}


	//プレイヤーがjoinした時に実行
	@EventHandler
	public void onplayerJoinEvent(PlayerJoinEvent event){
		//ジョインしたplayerを取得
		Player player = event.getPlayer();
		//プレイヤーのuuidを取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ作成
		PlayerData playerdata = sql.loadPlayerData(player);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataの作成に失敗しました。管理者に報告してください");
			return;
		}
		//playermapに追加
		playermap.put(uuid, playerdata);
		//join時とonenable時、プレイヤーデータを最新の状態に更新
		playerdata.UpdateonJoin(player);

		//初見さんへの処理
		if(!player.hasPlayedBefore()){
			//初見さんへのメッセージ文
			player.sendMessage(SeichiAssist.config.getLvMessage(1));
		}
	}

}
