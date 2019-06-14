package com.github.unchama.seichiassist.task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.github.unchama.util.collection.ImmutableListFactory;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

/**
 * 30分に1回まわしてる処理
 * @author unchama
 *
 */
public class EveryHalfHourTask extends BukkitRunnable{
	SeichiAssist plugin = SeichiAssist.Companion.getInstance();
	DatabaseGateway databaseGateway = SeichiAssist.Companion.getDatabaseGateway();
	HashMap<UUID,PlayerData> playermap = SeichiAssist.Companion.getPlayermap();
    static final List<ChatColor> color = ImmutableListFactory.of(ChatColor.DARK_PURPLE, ChatColor.BLUE, ChatColor.DARK_AQUA);

	public EveryHalfHourTask() {
	}


	@Override
	public void run() {
		//カウント値を０に設定
		int diggedPlayerCount = 0;
		//30分間の全プレイヤーの採掘量をallに格納
		int all = 0;


		Util.sendEveryMessage("--------------30分間整地ランキング--------------");

		//playermapに入っているすべてのプレイヤーデータについて処理
		for(PlayerData playerdata: SeichiAssist.Companion.getPlayermap().values()){
			//プレイヤー型を取得
			Player player = plugin.getServer().getPlayer(playerdata.getUuid());
			//プレイヤーがオンラインの時の処理
			if(player != null && playerdata.getLoaded()){
				//現在の統計量を取得
				long mines = playerdata.getTotalbreaknum();
				//現在の統計量を設定(after)
				playerdata.getHalfhourblock().after = mines;
				//前回との差を計算し設定(increase)
				playerdata.getHalfhourblock().setIncrease();
				//現在の統計量を設定（before)
				playerdata.getHalfhourblock().before = mines;

				//increaseが0超過の場合プレイヤー個人に個人整地量を通知
				if(playerdata.getHalfhourblock().increase > 0){
					player.sendMessage("あなたの整地量は " + ChatColor.AQUA + playerdata.getHalfhourblock().increase + ChatColor.WHITE + " でした");
				}

			}else if(!playerdata.getLoaded()){
				//debug用…このメッセージ視認後に大量集計されないかを確認する
				plugin.getServer().getConsoleSender().sendMessage("Apple Pen !");
				playerdata.getHalfhourblock().increase = 0;
			}else{
				//ﾌﾟﾚｲﾔｰがオフラインの時の処理
				//前回との差を０に設定
				playerdata.getHalfhourblock().increase = 0;
			}
			//allに30分間の採掘量を加算
			all += playerdata.getHalfhourblock().increase;
			//プレイヤーの30分の採掘量が1以上の時countを加算
			if(playerdata.getHalfhourblock().increase >= 1){
				diggedPlayerCount++;
			}
		}

		final List<PlayerData> entries = new ArrayList<>(SeichiAssist.Companion.getPlayermap().values());

		// ここで、0 -> 第一位、 1 -> 第二位、・・・n -> 第(n+1)位にする (つまり降順)
		entries.sort((o1, o2) -> {
			Long i1 = o1.getHalfhourblock().increase;
			Long i2 = o2.getHalfhourblock().increase;
			return Comparator.<Long>reverseOrder().compare(i1, i2);
		});

		Util.sendEveryMessage("全体の整地量は " + ChatColor.AQUA + all + ChatColor.WHITE + " でした");
		// * プレイヤーの数が負の数になることは絶対にない
		if (diggedPlayerCount != 0) {
			PlayerData e;
			final int size = entries.size();
			for (int i = 0; i <= 2; i++) { // 1から3位まで
				if (size == i) break;
				e = entries.get(i);
				Util.sendEveryMessage("整地量第" + (i + 1) + "位は" + color.get(i) + "[ Lv" + e.getLevel() +" ]" + e.getName() + ChatColor.WHITE + "で" + ChatColor.AQUA + e.getHalfhourblock().increase + ChatColor.WHITE + "でした");
			}
		}

		Util.sendEveryMessage("--------------------------------------------------");
	}
	public int getSendMessageAmount(){
		return SeichiAssist.Companion.getSeichiAssistConfig().getDefaultMineAmount()*30;
	}
}
