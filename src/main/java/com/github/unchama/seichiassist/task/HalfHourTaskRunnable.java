package com.github.unchama.seichiassist.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

/**
 * 30分に1回まわしてる処理
 * @author unchama
 *
 */
public class HalfHourTaskRunnable extends BukkitRunnable{
	SeichiAssist plugin = SeichiAssist.instance;
	Sql sql = SeichiAssist.sql;
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;

	public HalfHourTaskRunnable() {
	}


	@Override
	public void run() {
		//カウント値を０に設定
		int count = 0;
		//30分間の全プレイヤーの採掘量をallに格納
		int all = 0;


		Util.sendEveryMessage("--------------30分間整地ランキング--------------");

		//playermapに入っているすべてのプレイヤーデータについて処理
		for(PlayerData playerdata:SeichiAssist.playermap.values()){
			//プレイヤー型を取得
			Player player = plugin.getServer().getPlayer(playerdata.uuid);
			//プレイヤーがオンラインの時の処理
			if(player != null && playerdata.loaded){
				//現在の統計量を取得
				long mines = playerdata.totalbreaknum;
				//現在の統計量を設定(after)
				playerdata.halfhourblock.after = mines;
				//前回との差を計算し設定(increase)
				playerdata.halfhourblock.setIncrease();
				//現在の統計量を設定（before)
				playerdata.halfhourblock.before = mines;

				//increaseが0超過の場合プレイヤー個人に個人整地量を通知
				if(playerdata.halfhourblock.increase > 0){
					player.sendMessage("あなたの整地量は " + ChatColor.AQUA + playerdata.halfhourblock.increase + ChatColor.WHITE + " でした");
				}

			}else if(!playerdata.loaded){
				//debug用…このメッセージ視認後に大量集計されないかを確認する
				plugin.getServer().getConsoleSender().sendMessage("Apple Pen !");
				playerdata.halfhourblock.increase = 0;
			}else{
				//ﾌﾟﾚｲﾔｰがオフラインの時の処理
				//前回との差を０に設定
				playerdata.halfhourblock.increase = 0;
			}
			//allに30分間の採掘量を加算
			all += playerdata.halfhourblock.increase;
			//プレイヤーの30分の採掘量が1以上の時countを加算
			if(playerdata.halfhourblock.increase >= 1){
				count++;
			}
		}


		//Map.Entry のリストを作る
		List<Entry<UUID,PlayerData>> entries = new ArrayList<>(SeichiAssist.playermap.entrySet());

		//Comparator で Map.Entry の値を比較
		//比較関数
		Collections.sort(entries, (o1, o2) -> {
			Long i1 = o1.getValue().halfhourblock.increase;
			Long i2 = o2.getValue().halfhourblock.increase;
			return i2.compareTo(i1);//降順
		});

		Util.sendEveryMessage("全体の整地量は " + ChatColor.AQUA + all + ChatColor.WHITE + " でした");
		int countn = 1;
		for(Entry<UUID,PlayerData> e : entries){
			if (count <= 0 || countn >= 4) break;
			count --;

			if(countn == 1){
				Util.sendEveryMessage("整地量第1位は" + ChatColor.DARK_PURPLE + "[ Lv" + e.getValue().level +" ]" + e.getValue().name + ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue().halfhourblock.increase + ChatColor.WHITE + "でした");
			}else if(countn == 2){
				Util.sendEveryMessage("整地量第2位は" + ChatColor.BLUE + "[ Lv" + e.getValue().level +" ]" + e.getValue().name+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue().halfhourblock.increase + ChatColor.WHITE + "でした");
			}else if(countn == 3){
				Util.sendEveryMessage("整地量第3位は" + ChatColor.DARK_AQUA + "[ Lv" + e.getValue().level +" ]" + e.getValue().name+ ChatColor.WHITE + "で" + ChatColor.AQUA + e.getValue().halfhourblock.increase + ChatColor.WHITE + "でした");
			}
			countn++;
		}


		Util.sendEveryMessage("--------------------------------------------------");

	}
	public int getSendMessageAmount(){
		return SeichiAssist.config.getDefaultMineAmount()*30;
	}
}
