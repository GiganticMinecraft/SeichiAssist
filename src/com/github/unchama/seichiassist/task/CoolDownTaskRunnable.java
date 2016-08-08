package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

public class CoolDownTaskRunnable  extends BukkitRunnable{
	private HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap;
	private Player player;

	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	public CoolDownTaskRunnable(Player player) {
		this.player = player;
	}

	@Override
	public void run() {
		playermap = SeichiAssist.playermap;
		PlayerData playerdata = playermap.get(player.getUniqueId());
		playerdata.skillcanbreakflag = true;
		//デバッグ用
		if(SeichiAssist.DEBUG){
			player.sendMessage("クールダウンタイム終了");
		}
	}

}
