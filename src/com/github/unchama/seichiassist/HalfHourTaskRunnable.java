package com.github.unchama.seichiassist;

import static com.github.unchama.seichiassist.Config.*;
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
	private HashMap<Player,PlayerData> playermap = MultiSeichiEffect.playermap;

	private MineBlock mineblock;
	private int count = 1;

	public HalfHourTaskRunnable() {
	}


	@Override
	public void run() {
		count = 1;
		// MineBlock更新
		for(Player player : playermap.keySet()){
			mineblock = playermap.get(player);
			mineblock.setNow();
			mineblock.setIncrease();
			mineblock.setLast();
			playermap.put(player, mineblock);
			all += mineblock.getIncrease();
		}
		if(playermap.keySet().size()<3){
			return;
		}
		//Map.Entry のリストを作る
		List<Entry<Player, MineBlock>> entries = new ArrayList<Entry<Player, MineBlock>>(playermap.entrySet());

		//Comparator で Map.Entry の値を比較
		Collections.sort(entries, new Comparator<Entry<Player, MineBlock>>() {
		    //比較関数
		    @Override
		    public int compare(Entry<Player, MineBlock> o1, Entry<Player, MineBlock> o2) {
		    	Integer i1 = new Integer(o1.getValue().getIncrease());
		    	Integer i2 = new Integer(o2.getValue().getIncrease());
		    	return i2.compareTo(i1);    //降順
		    }
		});

		for (Entry<Player, MineBlock> e : entries) {
			if(count>3 || (e.getValue().getIncrease()==0) || all < getSendMessageAmount()){
				break;
			}
			if(count == 1){
				sendEveryMessage("---------------------------------");
				sendEveryMessage("この"+config.getNo1PlayerInterval()+"分間の総破壊量は " + ChatColor.AQUA + all + ChatColor.WHITE + "個でした");
				sendEveryMessage("破壊量第1位は" + ChatColor.DARK_PURPLE + e.getKey().getName().toString()+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue().getIncrease() + ChatColor.WHITE + "個でした");
			}else if(count == 2){
				sendEveryMessage("破壊量第2位は" + ChatColor.DARK_BLUE + e.getKey().getName().toString()+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue().getIncrease() + ChatColor.WHITE + "個でした");
			}else{
				sendEveryMessage("破壊量第3位は" + ChatColor.DARK_AQUA + e.getKey().getName().toString()+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue().getIncrease() + ChatColor.WHITE + "個でした");
				sendEveryMessage("---------------------------------");
			}
			count++;
		}


	}
	public int getSendMessageAmount(){
		return config.getDefaultMineAmount()*config.getNo1PlayerInterval();
	}
}
