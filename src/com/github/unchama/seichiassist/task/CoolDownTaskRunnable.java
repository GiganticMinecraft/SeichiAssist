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
	Boolean voteflag;
	boolean soundflag;
	boolean gachaflag;

	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	public CoolDownTaskRunnable(Player player,boolean voteflag,boolean soundflag,boolean gachaflag) {
		this.player = player;
		this.voteflag = voteflag;
		this.soundflag = soundflag;
		this.gachaflag = gachaflag;
		//UUIDを取得
		uuid = player.getUniqueId();
		//playerdataを取得
		playerdata = playermap.get(uuid);
		if(voteflag){
			playerdata.votecooldownflag = false;
		}else if(gachaflag){
			playerdata.gachacooldownflag = false;
		}else{
			playerdata.activeskilldata.skillcanbreakflag = false;
		}
	}

	@Override
	public void run() {
		if(voteflag){
			playerdata.votecooldownflag = true;
		}else if(gachaflag){
			playerdata.gachacooldownflag = true;
		}else{
			playerdata.activeskilldata.skillcanbreakflag = true;
			if(soundflag){
				player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, (float)0.5, (float)0.1);
			}
		}
		//デバッグ用
		if(SeichiAssist.DEBUG){
			player.sendMessage("クールダウンタイム終了");
		}
	}

}
