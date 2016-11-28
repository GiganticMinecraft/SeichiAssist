package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

public class PlayerDeathEventListener implements Listener {
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	SeichiAssist plugin = SeichiAssist.plugin;

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDeath(PlayerDeathEvent event){
		String msg = event.getDeathMessage();
		//オンラインの全てのプレイヤーを処理
		for(Player p : plugin.getServer().getOnlinePlayers()){
			//UUIDを取得
			UUID uuid = p.getUniqueId();
			//プレイヤーデータを取得
			PlayerData playerdata = playermap.get(uuid);
			//念のためエラー分岐
			if(playerdata == null){
				plugin.getLogger().warning(p.getName() + " -> PlayerData not found.");
				plugin.getLogger().warning("PlayerDeathEventListener.onDeath");
				continue;
			}
			//キルログ表示フラグがONのプレイヤーにのみ死亡メッセージを送信
			if(playerdata.dispkilllogflag){
				p.sendMessage(msg);
			}
		}
	}
}
