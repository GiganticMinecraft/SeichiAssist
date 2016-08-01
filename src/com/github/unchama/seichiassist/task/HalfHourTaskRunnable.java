package com.github.unchama.seichiassist.task;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.Util;
import com.github.unchama.seichiassist.data.MineBlock;

public class HalfHourTaskRunnable extends BukkitRunnable{
	SeichiAssist plugin = SeichiAssist.plugin;
	Sql sql = SeichiAssist.plugin.sql;

	public HalfHourTaskRunnable() {
	}


	@Override
	public void run() {
		int count = 0;
		int all = 0;
		ResultSet rs = sql.getTable();
		if(rs == null){
			Util.sendEveryMessage("テーブル取得に失敗しました。");
			return ;
		}
		try {
			while (rs.next()){
				String name = rs.getString("name");
				Player player = plugin.getServer().getPlayer(name);
				int increase = 0;
				if(player != null){
					int after = MineBlock.calcMineBlock(player);
					sql.insert("halfafter",after, name);
					increase = sql.selectint(name, "halfafter")-sql.selectint(name, "halfbefore");
					sql.insert("halfincrease",increase, name);
					sql.insert("halfbefore",after, name);
				}
				all += increase;
				if(increase >= getSendMessageAmount()){
					count++;
				}
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			Util.sendEveryMessage("統計の計算に失敗しました。");
			return;
		}

		if(count < 3 && !SeichiAssist.DEBUG){
			return;
		}

		//降順にしたrsを取得
		rs = sql.getRanking("halfincrease", 3);

		count = 1;
		try {
			while (rs.next()){

				if(count == 1){
					Util.sendEveryMessage("----------------------------------------");
					Util.sendEveryMessage("この30分間の総破壊量は " + ChatColor.AQUA + all + ChatColor.WHITE + "個でした");
					Util.sendEveryMessage("破壊量第1位は" + ChatColor.DARK_PURPLE + rs.getString("name")+ ChatColor.WHITE + "で" + ChatColor.AQUA + rs.getString("halfincrease") + ChatColor.WHITE + "個でした");
				}else if(count == 2){
					Util.sendEveryMessage("破壊量第2位は" + ChatColor.DARK_BLUE + rs.getString("name")+ ChatColor.WHITE + "で" + ChatColor.AQUA + rs.getString("halfincrease") + ChatColor.WHITE + "個でした");
				}else if(count == 3){
					Util.sendEveryMessage("破壊量第3位は" + ChatColor.DARK_AQUA + rs.getString("name")+ ChatColor.WHITE + "で" + ChatColor.AQUA + rs.getString("halfincrease") + ChatColor.WHITE + "個でした");
					Util.
					sendEveryMessage("----------------------------------------");
				}else{
					break;
				}
				count++;
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			Util.sendEveryMessage("ランキングの表示に失敗しました。");
			return ;
		}
	}
	public int getSendMessageAmount(){
		return SeichiAssist.config.getDefaultMineAmount()*30;
	}
}
