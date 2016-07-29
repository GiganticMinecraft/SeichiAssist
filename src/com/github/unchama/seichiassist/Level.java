package com.github.unchama.seichiassist;



import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.data.MineBlock;
import com.github.unchama.seichiassist.data.PlayerData;

public class Level{

	public static int calcPlayerLevel(Player player){
		//プレイヤー名を取得
		String name = Util.getName(player);
		//プレイヤーの統計値を取得
		int mines = 0;
		mines = MineBlock.calcMineBlock(player);
		//プレイヤーのデータを取得
		PlayerData playerdata = SeichiAssist.playermap.get(name);

		//現在のランクの次を取得
		int i = playerdata.level + 1;

		//ランクが上がらなくなるまで処理
		while(SeichiAssist.levellist.get(i).intValue() <= mines){
			playerdata.level = i;
			//レベルアップ時のメッセージ
			player.sendMessage(ChatColor.GOLD+"ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww【Lv("+(i-1)+")→Lv("+i+")】");
			//レベルアップ時の花火の打ち上げ
			Location loc = player.getLocation();
			Util.launchFireWorks(loc);
			String lvmessage = Config.getLvMessage(i);
			if(!(lvmessage.isEmpty())){
				player.sendMessage(ChatColor.AQUA+Config.getLvMessage(i));
			}
			i++;
		}


		return playerdata.level;
	}



	public static void setDisplayName(int i,Player p) {
		String name =Util.getName(p);

		if(p.isOp()){
			//管理人の場合
			name = ChatColor.RED + "<管理人>" + name + ChatColor.WHITE;
		}
		name =  "[ Lv" + i + " ]" + name;
		p.setDisplayName(name);
		p.setPlayerListName(name);
	}

	public static void updata(Player player) {
		int level = Level.calcPlayerLevel(player);
		Level.setDisplayName(level, player);
	}

	public static void setLevel(String name, int level) {
		PlayerData playerdata = SeichiAssist.playermap.get(name);
		playerdata.level = level;
	}
	public static int getLevel(String name) {
		PlayerData playerdata = SeichiAssist.playermap.get(name);
		return playerdata.level;
	}

	public static void reloadLevel(String name) {
		for(Player p : SeichiAssist.plugin.getServer().getOnlinePlayers()){
			if(Util.getName(p).equals(name)){
				int level = SeichiAssist.playermap.get(name).level;
				setDisplayName(level,p);
			}
		}

	}

}
