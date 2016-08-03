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
	HashMap<UUID,PlayerData> playermap;


	//プレイヤーがjoinした時に実行
	@EventHandler
	public void onplayerJoinEvent(PlayerJoinEvent event){
		//sqlを開く
		Sql sql = SeichiAssist.plugin.sql;
		Player player = event.getPlayer();
		PlayerData playerdata = new PlayerData(player);
		String name = Util.getName(player);
		playermap = SeichiAssist.playermap;
		if(!player.hasPlayedBefore()){
			//初見さんへのメッセージ文
			player.sendMessage(SeichiAssist.config.getLvMessage(1));
		}
		//ログインしたプレイヤーのデータが残っていなかった時にPlayerData作成
		if(!playermap.containsKey(player.getUniqueId())){
			playermap.put(player.getUniqueId(), playerdata);
			if(SeichiAssist.DEBUG){
				player.sendMessage("あたらしくプレイヤーデータを作成しました。");
			}
		}
		sql.insertname(SeichiAssist.PLAYERDATA_TABLENAME,name,player.getUniqueId());

		//更新したいものを更新
		playerdata.updata();
		playerdata.giveSorryForBug();

	}

}
