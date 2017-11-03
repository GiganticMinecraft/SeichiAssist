package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

public class PlayerDataUpdateOnJoinRunnable extends BukkitRunnable{

	//private SeichiAssist plugin = SeichiAssist.plugin;
	private HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	//private Sql sql = SeichiAssist.plugin.sql;

	final String table = SeichiAssist.PLAYERDATA_TABLENAME;

	String name;
	Player p;
	final UUID uuid;
	final String struuid;
	int i;
	boolean execLoad;
	PlayerData loginPlayerData;

	public PlayerDataUpdateOnJoinRunnable(PlayerData playerData,boolean execLoad) {
		name = playerData.name;
		uuid = playerData.uuid;
		p = Bukkit.getPlayer(uuid);
		struuid = uuid.toString().toLowerCase();
		i = 0;
		this.execLoad = execLoad;
		this.loginPlayerData = playerData;
	}

	@Override
	public void run() {

		//プレイヤーデータ取得
		PlayerData playerdata = playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			if(i >= 4){
	 			//諦める
				p.sendMessage(ChatColor.RED + "初回ロードに失敗しています。再接続してみても改善されない場合はお手数ですが管理人までご報告下さい");
	 			cancel();
	 			return;
	 		}else{
	 			//再試行
	 			p.sendMessage(ChatColor.YELLOW + "しばらくお待ちください…");
	 			if(execLoad){
	 				new LoadPlayerDataTaskRunnable(loginPlayerData).start();
	 			}
	 			i++;
	 			return;
	 		}
		}

		cancel();

		//同期処理をしないといけない部分ここから

		p.sendMessage(ChatColor.GREEN + "プレイヤーデータ取得完了");
		//join時とonenable時、プレイヤーデータを最新の状態に更新
		playerdata.updateonJoin(p);

		//同期処理をしないといけない部分ここまで
	}

}
