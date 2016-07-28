package com.github.unchama.seichiassist.listener;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

public class PlayerJoinListener implements Listener {
	HashMap<String,PlayerData> playermap;

	//プレイヤーがjoinした時に実行
	@EventHandler
	public void onplayerJoinEvent(PlayerJoinEvent event){
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		playermap = SeichiAssist.playermap;


		//ログインしたプレイヤーのデータが残っていなかった時にPlayerData作成
		if(!playermap.containsKey(name)){
			playermap.put(name, new PlayerData());
		}

		//playerのplayerdataを参照
		PlayerData playerdata = playermap.get(name);

		//更新したいものを更新
		playerdata.updata(player);
		playerdata.giveSorryForBug(player);

	}

}
