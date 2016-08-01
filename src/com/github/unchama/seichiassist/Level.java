package com.github.unchama.seichiassist;



import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.data.MineBlock;

public class Level{

	public static int calcPlayerLevel(Player player){
		//プレイヤー名を取得
		String name = Util.getName(player);
		//プレイヤーの統計値を取得
		int mines = 0;
		//sqlを開く
		Sql sql = SeichiAssist.plugin.sql;
		mines = MineBlock.calcMineBlock(player);
		//現在のランクの次を取得
		int i = sql.selectint(name, "level") + 1;

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
					player.sendMessage(ChatColor.AQUA+SeichiAssist.config.getLvMessage(i));
				}
			}
			i++;
		}
		sql.insert("level", i-1, name);


		return i-1;
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
		int level = calcPlayerLevel(player);
		setDisplayName(level, player);
	}

	public static void setLevel(String name, int level) {
		//sqlを開く
		Sql sql = SeichiAssist.plugin.sql;
		sql.insert("level", level, name);
	}
	public static int getLevel(String name) {
		//sqlを開く
		Sql sql = SeichiAssist.plugin.sql;
		return sql.selectint(name, "level");
	}

	public static void reloadLevel(String name) {
		//sqlを開く
		Sql sql = SeichiAssist.plugin.sql;
		for(Player p : SeichiAssist.plugin.getServer().getOnlinePlayers()){
			if(Util.getName(p).equals(name)){
				int level = sql.selectint(name, "level");
				setDisplayName(level,p);
			}
		}

	}

}
