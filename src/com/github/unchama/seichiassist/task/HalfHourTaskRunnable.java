package com.github.unchama.seichiassist.task;

import java.util.List;
import java.util.Map;

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
	private List<String> namelist;
	private Map<String,Integer> ranking;

	public HalfHourTaskRunnable() {
	}


	@Override
	public void run() {
		int count = 0;
		int all = 0;
		namelist = sql.getNameList();

		for(String name : namelist){
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
		if(count < 3 && !SeichiAssist.DEBUG){
			return;
		}

		//降順にしたrsを取得
		ranking = sql.getRanking("halfincrease", 3);

		count = 1;
		Util.sendEveryMessage("----------------------------------------");
		Util.sendEveryMessage("この30分間の総破壊量は " + ChatColor.AQUA + all + ChatColor.WHITE + "個でした");
		for(Map.Entry<String,Integer> e : ranking.entrySet()){
				Util.sendEveryMessage("破壊量第" + count + "位は" + ChatColor.DARK_PURPLE + e.getKey()+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue() + ChatColor.WHITE + "個でした");
			count++;
		}
		Util.sendEveryMessage("----------------------------------------");

	}
	public int getSendMessageAmount(){
		return SeichiAssist.config.getDefaultMineAmount()*30;
	}
}
