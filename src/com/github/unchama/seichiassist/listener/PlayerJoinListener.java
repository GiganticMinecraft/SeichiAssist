package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class PlayerJoinListener implements Listener {
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	Sql sql = SeichiAssist.plugin.sql;

	//プレイヤーがjoinした時に実行
	@EventHandler
	public void onplayerJoinEvent(PlayerJoinEvent event){
		//ジョインしたplayerを取得
		Player player = event.getPlayer();
		//プレイヤーのuuidを取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータを宣言
		PlayerData playerdata = null;
		//ログインしたプレイヤーのデータが残っていなかった時にPlayerData作成
		if(!playermap.containsKey(uuid)){
			//新しいplayerdataを作成
			playerdata = sql.loadPlayerData(player);
			//playermapに追加
			playermap.put(uuid, playerdata);
		}else{
			playerdata = playermap.get(uuid);
			//もし名前変更されていたら
			if(!Util.getName(player).equals(playerdata.name)){
				//すでにあるプレイヤーデータの名前を更新しておく
				playerdata.name = Util.getName(player);
			}
		}

		//統計量を取得
		int mines = Util.calcMineBlock(player);
		playerdata.updata(player,mines);
		playerdata.NotifySorryForBug(player);
		//初見さんへの処理
		if(!player.hasPlayedBefore()){
			//初見さんへのメッセージ文
			player.sendMessage(SeichiAssist.config.getLvMessage(1));
		}
	}

}
