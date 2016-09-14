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
import com.github.unchama.seichiassist.util.Util;

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
			//満員時、キック対象のプレイヤーを検索
			for(Player p : plugin.getServer().getOnlinePlayers()){
				if(p.hasPermission("SeichiAssist.fullstay")){
					//権限持ちはスルー
					continue;
				}
				//UUIDを取得
				UUID uuid = p.getUniqueId();
				//playerdata取得
				PlayerData playerdata = playermap.get(uuid);
				//念のためエラー分岐
				if(playerdata == null){
					p.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
					plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[満員時キック処理]でエラー発生");
					plugin.getLogger().warning(p.getName() + "のplayerdataがありません。開発者に報告してください");
					continue;
				}
				//閾値を超えていたら追い出しを実行して処理を終了
				if(playerdata.idletime >= 10){
					plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "満員の為放置プレイヤーを追い出します");
					Util.sendEveryMessage(ChatColor.YELLOW + "満員の為放置プレイヤーを追い出します");
					p.kickPlayer("放置プレイヤーはキックされます(満員時のみ)。再度ログインすることが可能です。");
					e.allow();
					return;
				}
			}
			//キック対象が居なかったら…

			//権限持ちはログインさせる
			if(e.getPlayer().hasPermission("SeichiAssist.fullstay")){
				e.allow();
				return;
			}

			//メッセージ表示
			e.disallow(PlayerLoginEvent.Result.KICK_FULL, "満員かつ放置プレイヤーが居なかった為入れませんでした。しばらく経ってから再度お試し下さい");
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
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[join処理]でエラー発生");
			plugin.getLogger().warning(player.getName() + "のplayerdataの作成に失敗しました。開発者に報告してください");
			return;
		}
		//playermapに追加
		playermap.put(uuid, playerdata);
		//join時とonenable時、プレイヤーデータを最新の状態に更新
		playerdata.updateonJoin(player);

		//初見さんへの処理
		if(!player.hasPlayedBefore()){
			//初見さんへのメッセージ文
			player.sendMessage(SeichiAssist.config.getLvMessage(1));
		}
	}

}
