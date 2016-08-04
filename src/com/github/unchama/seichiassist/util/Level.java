package com.github.unchama.seichiassist.util;



import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

public class Level{
	//プレイヤーの統計量、整地レベルを更新する。
	public static void updata(Player player,int mines) {
		int level = calcPlayerLevel(player,mines);
		setDisplayName(level, player);
	}
	//プレイヤーレベルを計算し、更新する。
	public static int calcPlayerLevel(Player player,int mines){
		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータを取得
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//現在のランクの次を取得
		int i = playerdata.level + 1;

		//ランクが上がらなくなるまで処理
		while(SeichiAssist.levellist.get(i).intValue() <= mines){
			//レベルアップ時のメッセージ
			if(!SeichiAssist.DEBUG){
				player.sendMessage(ChatColor.GOLD+"ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww【Lv("+(i-1)+")→Lv("+i+")】");
				//レベルアップ時の花火の打ち上げ
				Location loc = player.getLocation();
				Util.launchFireWorks(loc);
				String lvmessage = SeichiAssist.config.getLvMessage(i);
				if(!(lvmessage.isEmpty())){
					player.sendMessage(ChatColor.AQUA+lvmessage);
				}
			}
			i++;
		}
		playerdata.level = i-1;


		return i-1;
	}
	//表示される名前に整地レベルを追加
	public static void setDisplayName(int level,Player p) {
		String name =Util.getName(p);

		if(p.isOp()){
			//管理人の場合
			name = ChatColor.RED + "<管理人>" + name + ChatColor.WHITE;
		}
		name =  "[ Lv" + level + " ]" + name;
		p.setDisplayName(name);
		p.setPlayerListName(name);
	}
	//プレイヤーのレベルを指定された値に設定
	public static void setLevel(UUID uuid, int level) {
		//プレイヤーデータを取得
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		playerdata.level = level;
	}
	public static int getLevel(UUID uuid) {
		//プレイヤーデータを取得
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		return playerdata.level;
	}


}
