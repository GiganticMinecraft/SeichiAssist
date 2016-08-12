package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

public class CoolDownTaskRunnable  extends BukkitRunnable{
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	private Player player;
	UUID uuid;
	PlayerData playerdata;

	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	public CoolDownTaskRunnable(Player player) {
		this.player = player;
		//UUIDを取得
		uuid = player.getUniqueId();
		//playerdataを取得
		playerdata = playermap.get(uuid);
		playerdata.skillcanbreakflag = false;
	}

	@Override
	public void run() {
		playerdata.skillcanbreakflag = true;
		player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, (float)0.5, (float)0.1);
		//デバッグ用
		if(SeichiAssist.DEBUG){
			player.sendMessage("クールダウンタイム終了");
		}
	}

}
