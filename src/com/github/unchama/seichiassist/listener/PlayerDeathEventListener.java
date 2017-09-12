package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
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

		// 1周年記念
		anniversary(event.getEntity());
	}

	// 1周年記念
	private void anniversary(Player p) {
		PlayerData playerdata = playermap.get(p.getUniqueId());
		if (playerdata.anniversary) {
			if (p.getInventory().firstEmpty() == -1) {
				p.sendMessage("インベントリが一杯の為、アイテムを入手出来ませんでした。");
				p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1f, 1f);
			} else {
				String command = "give " + p.getName() + " skull 1 3 {display:{Name:\"マインちゃん\",Lore:[\"\", \"" + ChatColor.YELLOW + "整地サーバ1周年記念だよ！\"]},SkullOwner:{Id:\"fac7c46e-3e89-4249-bef6-948d5eb528c9\",Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDhmNTQ0OGI0ZDg4ZTQwYjE0YzgyOGM2ZjFiNTliMzg1NDVkZGE5MzNlNzNkZmYzZjY5NWU2ZmI0Mjc4MSJ9fX0=\"}]}}}";
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
				p.sendMessage("整地サーバ1周年の記念品を入手しました。");
				p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1f, 1f);
				playerdata.anniversary = false;
				SeichiAssist.sql.setAnniversary(false, p.getUniqueId());
			}
		}
	}
}
