package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

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

	public PlayerDataUpdateOnJoinRunnable(Player _p) {
		p = _p;
		name = Util.getName(p);
		uuid = p.getUniqueId();
		struuid = uuid.toString().toLowerCase();
		i = 0;
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
