package com.github.unchama.seichiassist.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.data.MineBlock;
import com.github.unchama.seichiassist.data.PlayerData;

public class HalfHourTaskRunnable extends BukkitRunnable{
	private HashMap<String,PlayerData> playermap = SeichiAssist.playermap;
	SeichiAssist plugin = SeichiAssist.plugin;
	public HalfHourTaskRunnable() {
	}


	@Override
	public void run() {
		int count = 0;
		int all = 0;
		for (String name : playermap.keySet()){
			PlayerData playerdata = playermap.get(name);
			Player player = plugin.getServer().getPlayer(name);
			MineBlock mineblock  = playerdata.halfhourblock;
			if(player != null){
				mineblock.after = Util.calcMineBlock(player);
				mineblock.setIncrease();
				mineblock.before = mineblock.after;
			}else{
				mineblock.increase = 0;
			}
			all += mineblock.increase;
			if(mineblock.increase >= getSendMessageAmount()){
				count++;
			}
		}


		if(count < 3){
			return;
		}

		//Map.Entry のリストを作る
		List<Entry<String,PlayerData>> entries = new ArrayList<Entry<String, PlayerData>>(playermap.entrySet());

		//Comparator で Map.Entry の値を比較
		Collections.sort(entries, new Comparator<Entry<String, PlayerData>>() {
		    //比較関数
		    @Override
		    public int compare(Entry<String, PlayerData> o1, Entry<String, PlayerData> o2) {
		    	Integer i1 = new Integer(o1.getValue().halfhourblock.increase);
		    	Integer i2 = new Integer(o2.getValue().halfhourblock.increase);
		    	return i2.compareTo(i1);//降順
		    }
		});

		count = 1;
		for (Entry<String, PlayerData> e : entries) {

			if(count == 1){
				Util.sendEveryMessage("----------------------------------------");
				Util.sendEveryMessage("この30分間の総破壊量は " + ChatColor.AQUA + all + ChatColor.WHITE + "個でした");
				Util.sendEveryMessage("破壊量第1位は" + ChatColor.DARK_PURPLE + e.getKey()+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue().halfhourblock.increase + ChatColor.WHITE + "個でした");
			}else if(count == 2){
				Util.sendEveryMessage("破壊量第2位は" + ChatColor.DARK_BLUE + e.getKey()+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue().halfhourblock.increase + ChatColor.WHITE + "個でした");
			}else if(count == 3){
				Util.sendEveryMessage("破壊量第3位は" + ChatColor.DARK_AQUA + e.getKey()+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue().halfhourblock.increase + ChatColor.WHITE + "個でした");
				Util.
				sendEveryMessage("----------------------------------------");
			}else{
				break;
			}
			count++;
		}
	}
	public int getSendMessageAmount(){
		return Config.getDefaultMineAmount()*30;
	}
}
