package com.github.unchama.seichiassist.listener;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Created by karayuu on 2018/06/17
 */

public class PlayerChatEventListener implements Listener {

	@EventHandler(priority=EventPriority.LOW)
	public void setSubHomeName(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		PlayerData data = SeichiAssist.Companion.getPlayermap().get(player.getUniqueId());

		if (!data.isSubHomeNameChange()) {
			return;
		}

		int n = data.getSetHomeNameNum();

		data.setSubHomeName(event.getMessage(), n);

		player.sendMessage(ChatColor.GREEN + "サブホームポイント" + (n+1) + "の名前を");
		player.sendMessage(ChatColor.GREEN + event.getMessage() + "に更新しました");

		data.setSubHomeNameChange(false);
		event.setCancelled(true);
	}

}
