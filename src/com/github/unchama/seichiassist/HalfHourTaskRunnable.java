package com.github.unchama.seichiassist;

import static com.github.unchama.seichiassist.Util.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class HalfHourTaskRunnable extends BukkitRunnable{
	private HashMap<Player,PlayerData> playermap;
	Player player;
	SeichiAssist plugin;
	PlayerData playerdata;
	private int count;
	private int all;

	public HalfHourTaskRunnable(SeichiAssist _plugin) {
		plugin = _plugin;
	}


	@Override
	public void run() {
		playermap = SeichiAssist.playermap;
		plugin = SeichiAssist.plugin;
		count = 1;

		for (Player p : plugin.getServer().getOnlinePlayers()){
			if(!playermap.containsKey(p)){
				playermap.put(p,new PlayerData(p));
			}
			player = p;
			playerdata = playermap.get(player);
			MineBlock mineblock  = playerdata.halfhourblock;
			mineblock.after = Util.calcMineBlock(player);
			mineblock.increase = mineblock.after - mineblock.before;
			mineblock.before = mineblock.after;
			all += mineblock.increase;
		}
		if(plugin.getServer().getOnlinePlayers().size() < 3){
			return;
		}
		//Map.Entry のリストを作る
		List<Entry<Player,PlayerData>> entries = new ArrayList<Entry<Player, PlayerData>>(playermap.entrySet());

		//Comparator で Map.Entry の値を比較
		Collections.sort(entries, new Comparator<Entry<Player, PlayerData>>() {
		    //比較関数
		    @Override
		    public int compare(Entry<Player, PlayerData> o1, Entry<Player, PlayerData> o2) {
		    	Integer i1 = new Integer(o1.getValue().halfhourblock.increase);
		    	Integer i2 = new Integer(o2.getValue().halfhourblock.increase);
		    	return i2.compareTo(i1);    //降順
		    }
		});

		for (Entry<Player, PlayerData> e : entries) {
			if(count>3 || (e.getValue().halfhourblock.increase==0) || all < getSendMessageAmount()){
				break;
			}
			if(count == 1){
				sendEveryMessage("---------------------------------");
				sendEveryMessage("この30分間の総破壊量は " + ChatColor.AQUA + all + ChatColor.WHITE + "個でした");
				sendEveryMessage("破壊量第1位は" + ChatColor.DARK_PURPLE + e.getKey().getName().toString()+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue().halfhourblock.increase + ChatColor.WHITE + "個でした");
			}else if(count == 2){
				sendEveryMessage("破壊量第2位は" + ChatColor.DARK_BLUE + e.getKey().getName().toString()+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue().halfhourblock.increase + ChatColor.WHITE + "個でした");
			}else{
				sendEveryMessage("破壊量第3位は" + ChatColor.DARK_AQUA + e.getKey().getName().toString()+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue().halfhourblock.increase + ChatColor.WHITE + "個でした");
				sendEveryMessage("---------------------------------");
			}
			count++;
		}


	}
	public int getSendMessageAmount(){
		return Config.getDefaultMineAmount()*30;
	}
}
