package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

public class CoolDownTask extends BukkitRunnable{
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
	public CoolDownTask(Player player, boolean voteflag, boolean soundflag, boolean gachaflag) {
		this.player = player;
		this.voteflag = voteflag;
		this.soundflag = soundflag;
		this.gachaflag = gachaflag;
		//UUIDを取得
		uuid = player.getUniqueId();
		//playerdataを取得
		playerdata = playermap.get(uuid);
		if(voteflag){
			playerdata.setVotecooldownflag(false);
		}else if(gachaflag){
			playerdata.setGachacooldownflag(false);
		}else{
			playerdata.getActiveskilldata().skillcanbreakflag = false;
		}
	}

	// 拡張版
	public CoolDownTask(Player player, String tag) {
		this.player = player;
		//UUIDを取得
		uuid = player.getUniqueId();
		//playerdataを取得
		playerdata = playermap.get(uuid);
		switch (tag) {
		case VOTE:
			voteflag = true;
			playerdata.setVotecooldownflag(false);
			break;
		case SOUND:
			soundflag = true;
			playerdata.getActiveskilldata().skillcanbreakflag = false;
			break;
		case GACHA:
			gachaflag = true;
			playerdata.setGachacooldownflag(false);
			break;
		case SHAREINV:
			shareinvflag = true;
			playerdata.setShareinvcooldownflag(false);
			break;
		default:
			// ベースに合わせて念のためdefaultはsoundに合わせておく
			soundflag = true;
			playerdata.getActiveskilldata().skillcanbreakflag = false;
			break;
		}
	}

	@Override
	public void run() {
		if(voteflag){
			playerdata.setVotecooldownflag(true);
		}else if(gachaflag){
			playerdata.setGachacooldownflag(true);
		}else if(shareinvflag){
			playerdata.setShareinvcooldownflag(true);
		}else{
			playerdata.getActiveskilldata().skillcanbreakflag = true;
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
