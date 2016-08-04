package com.github.unchama.seichiassist.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.MineBlock;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class HalfHourTaskRunnable extends BukkitRunnable{
	SeichiAssist plugin = SeichiAssist.plugin;
	Sql sql = SeichiAssist.plugin.sql;

	public HalfHourTaskRunnable() {
	}


	@Override
	public void run() {
		//カウント値を０に設定
		int count = 0;
		//30分間の全プレイヤーの採掘量をallに格納
		int all = 0;


		//playermapに入っているすべてのプレイヤーデータについて処理
		for(PlayerData playerdata:SeichiAssist.playermap.values()){
			//プレイヤー型を取得
			Player player = plugin.getServer().getPlayer(playerdata.name);
			//プレイヤーがオンラインの時の処理
			if(player != null){
				//現在の統計量を取得
				int mines = MineBlock.calcMineBlock(player);
				//現在の統計量を設定(after)
				playerdata.halfhourblock.after = mines;
				//前回との差を計算し設定(increase)
				playerdata.halfhourblock.setIncrease();
				//現在の統計量を設定（before)
				playerdata.halfhourblock.before = mines;
			}else{
				//ﾌﾟﾚｲﾔｰがオフラインの時の処理
				//前回との差を０に設定
				playerdata.halfhourblock.increase = 0;
			}
			//allに30分間の採掘量を加算
			all += playerdata.halfhourblock.increase;
			//プレイヤーの30分の採掘量が一定値以上の時countを加算
			if(playerdata.halfhourblock.increase >= getSendMessageAmount()){
				count++;
			}
		}
		//カウントの値が３よりちいさい時処理を終了
		if(count < 3 && !SeichiAssist.DEBUG){
			return;
		}


		//Map.Entry のリストを作る
		List<Entry<UUID,PlayerData>> entries = new ArrayList<Entry<UUID,PlayerData>>(SeichiAssist.playermap.entrySet());

		//Comparator で Map.Entry の値を比較
		Collections.sort(entries, new Comparator<Entry<UUID,PlayerData>>() {
		    //比較関数
		    @Override
		    public int compare(Entry<UUID,PlayerData> o1, Entry<UUID,PlayerData> o2) {
		    	Integer i1 = new Integer(o1.getValue().halfhourblock.increase);
		    	Integer i2 = new Integer(o2.getValue().halfhourblock.increase);
		    	return i2.compareTo(i1);//降順
		    }
		});
		//カウントを1に再設定
		count = 1;
		Util.sendEveryMessage("----------------------------------------");
		Util.sendEveryMessage("この30分間の総破壊量は " + ChatColor.AQUA + all + ChatColor.WHITE + "個でした");
		for(Entry<UUID,PlayerData> e : entries){
			if(count == 1){
				Util.sendEveryMessage("破壊量第1位は" + ChatColor.DARK_PURPLE + e.getValue().name + ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue().halfhourblock.increase + ChatColor.WHITE + "個でした");
			}else if(count == 2){
				Util.sendEveryMessage("破壊量第2位は" + ChatColor.DARK_BLUE + e.getValue().name+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue().halfhourblock.increase + ChatColor.WHITE + "個でした");
			}else if(count == 3){
				Util.sendEveryMessage("破壊量第3位は" + ChatColor.DARK_AQUA + e.getValue().name+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue().halfhourblock.increase + ChatColor.WHITE + "個でした");
			}
			count++;
		}
		Util.sendEveryMessage("----------------------------------------");

	}
	public int getSendMessageAmount(){
		return SeichiAssist.config.getDefaultMineAmount()*30;
	}
}
