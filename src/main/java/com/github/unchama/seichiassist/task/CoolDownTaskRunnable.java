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
	boolean voteflag = false;
    public static final String VOTE = "VOTE";
	boolean soundflag = false;
	public static final String SOUND = "SOUND";
	boolean gachaflag = false;
	public static final String GACHA = "GACHA";
	boolean shareinvflag = false;
	public static final String SHAREINV = "SHAREINV";

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

	// 拡張版
	public CoolDownTaskRunnable(Player player, String tag) {
		this.player = player;
		//UUIDを取得
		uuid = player.getUniqueId();
		//playerdataを取得
		playerdata = playermap.get(uuid);
		switch (tag) {
		case VOTE:
			voteflag = true;
			playerdata.votecooldownflag = false;
			break;
		case SOUND:
			soundflag = true;
			playerdata.activeskilldata.skillcanbreakflag = false;
			break;
		case GACHA:
			gachaflag = true;
			playerdata.gachacooldownflag = false;
			break;
		case SHAREINV:
			shareinvflag = true;
			playerdata.shareinvcooldownflag = false;
			break;
		default:
			// ベースに合わせて念のためdefaultはsoundに合わせておく
			soundflag = true;
			playerdata.activeskilldata.skillcanbreakflag = false;
			break;
		}
	}

	@Override
	public void run() {
		if(voteflag){
			playerdata.votecooldownflag = true;
		}else if(gachaflag){
			playerdata.gachacooldownflag = true;
		}else if(shareinvflag){
			playerdata.shareinvcooldownflag = true;
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
