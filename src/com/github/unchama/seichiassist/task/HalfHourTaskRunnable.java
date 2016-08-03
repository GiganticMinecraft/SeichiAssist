package com.github.unchama.seichiassist.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.MineBlock;
import com.github.unchama.seichiassist.util.Util;

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
		namelist = sql.getNameList(SeichiAssist.PLAYERDATA_TABLENAME);

		for(String name : namelist){
			Player player = plugin.getServer().getPlayer(name);
			int increase = 0;
			if(player != null){
				int after = MineBlock.calcMineBlock(player);
				sql.insert(SeichiAssist.PLAYERDATA_TABLENAME,"halfafter",after, name);
				increase = sql.selectint(SeichiAssist.PLAYERDATA_TABLENAME,name, "halfafter")-sql.selectint(SeichiAssist.PLAYERDATA_TABLENAME,name, "halfbefore");
				sql.insert(SeichiAssist.PLAYERDATA_TABLENAME,"halfincrease",increase, name);
				sql.insert(SeichiAssist.PLAYERDATA_TABLENAME,"halfbefore",after, name);
			}else{
				increase = 0;
				sql.insert(SeichiAssist.PLAYERDATA_TABLENAME,"halfincrease",increase, name);
				int after = sql.selectint(SeichiAssist.PLAYERDATA_TABLENAME,name, "halfafter");
				sql.insert(SeichiAssist.PLAYERDATA_TABLENAME,"halfbefore",after, name);
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
		ranking = sql.getRanking(SeichiAssist.PLAYERDATA_TABLENAME,"halfincrease", 3);
		//Map.Entry のリストを作る
		List<Entry<String,Integer>> entries = new ArrayList<Entry<String, Integer>>(ranking.entrySet());

		//Comparator で Map.Entry の値を比較
		Collections.sort(entries, new Comparator<Entry<String, Integer>>() {
		    //比較関数
		    @Override
		    public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
		    	Integer i1 = new Integer(o1.getValue());
		    	Integer i2 = new Integer(o2.getValue());
		    	return i2.compareTo(i1);//降順
		    }
		});
		count = 1;
		Util.sendEveryMessage("----------------------------------------");
		Util.sendEveryMessage("この30分間の総破壊量は " + ChatColor.AQUA + all + ChatColor.WHITE + "個でした");
		for(Entry<String, Integer> e : entries){
			if(count == 1){
				Util.sendEveryMessage("破壊量第1位は" + ChatColor.DARK_PURPLE + e.getKey()+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue() + ChatColor.WHITE + "個でした");
			}else if(count == 2){
				Util.sendEveryMessage("破壊量第2位は" + ChatColor.DARK_BLUE + e.getKey()+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue() + ChatColor.WHITE + "個でした");
			}else if(count == 3){
				Util.sendEveryMessage("破壊量第3位は" + ChatColor.DARK_AQUA + e.getKey()+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue() + ChatColor.WHITE + "個でした");
			}
			count++;
		}
		Util.sendEveryMessage("----------------------------------------");

	}
	public int getSendMessageAmount(){
		return SeichiAssist.config.getDefaultMineAmount()*30;
	}
}
