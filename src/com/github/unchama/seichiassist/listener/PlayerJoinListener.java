package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

public class PlayerJoinListener implements Listener {
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;


	//プレイヤーがjoinした時に実行
	@EventHandler
	public void onplayerJoinEvent(PlayerJoinEvent event){
		//ジョインしたplayerを取得
		Player player = event.getPlayer();
		//プレイヤーのuuidを取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータを宣言
		PlayerData playerdata;
		//ログインしたプレイヤーのデータが残っていなかった時にPlayerData作成
		if(!playermap.containsKey(uuid)){
			//新しいplayerdataを作成
			playerdata = new PlayerData(player);
			//playermapに追加
			playermap.put(player.getUniqueId(), playerdata);
			//デバッグ処理
			if(SeichiAssist.DEBUG){
				player.sendMessage("あたらしくプレイヤーデータを作成しました。");
			}
		}else{
			playerdata = playermap.get(uuid);
			//UUIDは同じだがplayernameが異なっているときに現在のplayernameに更新
			playerdata.renewName(player);
		}
		//初見さんへの処理
		if(!player.hasPlayedBefore()){
			//初見さんへのメッセージ文
			player.sendMessage(SeichiAssist.config.getLvMessage(1));
		}
	}

}
