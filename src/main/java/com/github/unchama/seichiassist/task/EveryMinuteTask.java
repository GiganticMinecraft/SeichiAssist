package com.github.unchama.seichiassist.task;


import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.achievement.SeichiAchievement;
import com.github.unchama.seichiassist.data.EffectData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

/**
 * 1分に1回回してる処理
 * @author unchama
 *
 */
public class EveryMinuteTask extends BukkitRunnable{
	private SeichiAssist plugin = SeichiAssist.Companion.getInstance();
	private HashMap<UUID, PlayerData> playermap = SeichiAssist.Companion.getPlayermap();
	private Config config = SeichiAssist.Companion.getSeichiAssistConfig();

	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	public EveryMinuteTask() {

	}

	@Override
	public void run() {
		// プレイヤーの１分間の処理を実行

		//playermapが空の時return
		if(playermap.isEmpty()){
			return;
		}

		//オンラインプレイヤーの人数を取得
		int onlinenums = plugin.getServer().getOnlinePlayers().size();

		//プレイヤーマップに記録されているすべてのplayerdataについての処理
		for(PlayerData playerdata : playermap.values()){
			//プレイヤーのオンラインオフラインに関係なく処理
			//エフェクトデータの持続時間を1200tick引いて、０以下のものを削除
			playerdata.calcEffectData();

			//プレイヤーがオフラインの時処理を終了、次のプレイヤーへ
			if(playerdata.isOffline()){
				continue;
			}
			//プレイﾔｰが必ずオンラインと分かっている処理
			//プレイヤーを取得
			Player player = plugin.getServer().getPlayer(playerdata.getUuid());

			//放置判定
			if(player.getLocation().equals(playerdata.getLoc())){
				// idletime加算
				playerdata.setIdletime(playerdata.getIdletime() + 1);
			}else{
				// 現在地点再取得
				playerdata.setLoc(player.getLocation());
				// idletimeリセット
				playerdata.setIdletime(0);
			}

			//プレイヤー名を取得
			String name = Util.getName(player);
			//総整地量を更新(返り血で重み分け済みの1分間のブロック破壊量が返ってくる)
			int increase = playerdata.calcMineBlock(player);
			//Levelを設定(必ず総整地量更新後に実施！)
			playerdata.updateLevel(player);
			//activeskillpointを設定
			playerdata.getActiveskilldata().updateActiveSkillPoint(player, playerdata.getLevel());
			//総プレイ時間更新
			playerdata.calcPlayTick(player);

			//スターレベル更新
			playerdata.calcStarLevel(player);

			//effectの大きさ
			double amplifier;
			//effectのメッセージ
			//１分間のブロック破壊量による上昇
			amplifier = (double) increase * config.getMinuteMineSpeed();
			playerdata.getEffectdatalist().add(new EffectData(amplifier,2));

			//プレイヤー数による上昇
			amplifier = (double) onlinenums * config.getLoginPlayerMineSpeed();
			playerdata.getEffectdatalist().add(new EffectData(amplifier,1));

			//effect追加の処理
			//実際に適用されるeffect量
			int minespeedlv = 0;

			//effectflag ONの時のみ実行
			if (playerdata.getEffectflag() != 5) {
				//合計effect量
				double sum = 0;
				//最大持続時間
				int maxduration = 0;
				//effectdatalistにある全てのeffectについて計算
				for(EffectData ed : playerdata.getEffectdatalist()){
					//effect量を加算
					sum += ed.amplifier;
					//持続時間の最大値を取得
					if(maxduration < ed.duration){
						maxduration = ed.duration;
					}
				}
				//実際のeffect値をsum-1の切り捨て整数値に設定
				minespeedlv = (int)(sum - 1);

				//effect上限値を判定
				int maxSpeed = 0;
				if (playerdata.getEffectflag() == 0) {
					maxSpeed = 25565;
				} else if (playerdata.getEffectflag() == 1) {
					maxSpeed = 127;
				} else if(playerdata.getEffectflag() == 2) {
					maxSpeed = 200;
				} else if(playerdata.getEffectflag() == 3) {
					maxSpeed = 400;
				} else if(playerdata.getEffectflag() == 4) {
					maxSpeed = 600;
				}

				//effect追加の処理
				//実際のeffect値が0より小さいときはeffectを適用しない
				if(minespeedlv < 0 || maxSpeed == 0){
					player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0, false, false), true);
				}else{
					if(minespeedlv > maxSpeed) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, maxduration, maxSpeed, false, false), true);
					}else{
						player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, maxduration, minespeedlv, false, false), true);
					}
				}

				//プレイヤーデータを更新
				playerdata.setMinespeedlv(minespeedlv);
			}

			//プレイヤーにメッセージ送信
			if(playerdata.getLastminespeedlv() != minespeedlv || playerdata.getMessageflag()){//前の上昇量と今の上昇量が違うか内訳表示フラグがオンの時告知する
				player.sendMessage(ChatColor.YELLOW + "★" + ChatColor.WHITE + "採掘速度上昇レベルが" + ChatColor.YELLOW + (minespeedlv+1) + ChatColor.WHITE +"になりました");
				if(playerdata.getMessageflag()){
					player.sendMessage("----------------------------内訳-----------------------------");
					for(EffectData ed : playerdata.getEffectdatalist()){
						player.sendMessage(ChatColor.RESET + "" +  ChatColor.RED + "" + ed.EDtoString(ed.id,ed.duration,ed.amplifier));
					}
					player.sendMessage("-------------------------------------------------------------");
				}
			}

			//プレイヤーデータを更新
			playerdata.setLastminespeedlv(minespeedlv);

			/*
			 * ガチャ券付与の処理
			 */

			//ガチャポイントに合算
			playerdata.setGachapoint(playerdata.getGachapoint() + increase);

			if(playerdata.getGachapoint() >= config.getGachaPresentInterval() && playerdata.getGachaflag()){
				ItemStack skull = Util.getskull(name);
				playerdata.setGachapoint(playerdata.getGachapoint() - config.getGachaPresentInterval());
				if(player.getInventory().contains(skull) || !Util.isPlayerInventoryFull(player)){
					Util.addItem(player,skull);
					player.sendMessage(ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "プレゼントフォーユー。右クリックで使えるゾ");
				}else{
					Util.dropItem(player,skull);
					player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
					player.sendMessage(ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "がドロップしました。右クリックで使えるゾ");
				}
			}else{
				if(increase != 0 && playerdata.getGachaflag()){
					player.sendMessage("あと" + ChatColor.AQUA + (config.getGachaPresentInterval()-(playerdata.getGachapoint() % config.getGachaPresentInterval())) + ChatColor.WHITE + "ブロック整地すると" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "獲得ダヨ");
				}
			}
			//プレイヤーデータを更新
			playerdata.setLastgachapoint(playerdata.getGachapoint());




			/*
			 * 実績解除判定
			 */
			//No1000系統の解禁チェック
			int checkNo = 1001 ;
			for(;checkNo < 1013 ;){
				if(!playerdata.getTitleFlags().get(checkNo)){
					SeichiAchievement.tryAchieve(player,checkNo);
				}
				checkNo ++ ;
			}
			//No3000系統の解禁チェック
			checkNo = 3001 ;
			for(;checkNo < 3020 ;){
				if(!playerdata.getTitleFlags().get(checkNo)){
					SeichiAchievement.tryAchieve(player,checkNo);
				}
				checkNo ++ ;
			}
			//No4000系統の解禁チェック
			checkNo = 4001 ;
			for(;checkNo < 4024 ;){
				if(!playerdata.getTitleFlags().get(checkNo)){
					SeichiAchievement.tryAchieve(player,checkNo);
				}
				checkNo ++ ;
			}
			//No5000系統の解禁チェック
			checkNo = 5001 ;
			for(;checkNo < 5009 ;){
				if(!playerdata.getTitleFlags().get(checkNo)){
					SeichiAchievement.tryAchieve(player,checkNo);
				}
				checkNo ++ ;
			}
			//No5100系統の解禁チェック
			checkNo = 5101 ;
			for(;checkNo < 5121 ;){
				if(!playerdata.getTitleFlags().get(checkNo)){
					SeichiAchievement.tryAchieve(player,checkNo);
				}
				checkNo ++ ;
			}
			//No6000系統の解禁チェック
			checkNo = 6001 ;
			for(;checkNo < 6009 ;){
				if(!playerdata.getTitleFlags().get(checkNo)){
					SeichiAchievement.tryAchieve(player,checkNo);
				}
				checkNo ++ ;
			}
			//No8000系統の解禁チェック
			checkNo = 8001 ;
			for(;checkNo < 8003 ;){
				if(!playerdata.getTitleFlags().get(checkNo)){
					SeichiAchievement.tryAchieve(player,checkNo);
				}
				checkNo ++ ;
			}

			//投票妖精関連
			if (playerdata.getUsingVotingFairy()) {
				VotingFairyTask.run(player);
			}

			//GiganticBerserk
			playerdata.setGBcd(0);

		}

	}
}
