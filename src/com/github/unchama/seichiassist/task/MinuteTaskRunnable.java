package com.github.unchama.seichiassist.task;


import java.util.HashMap;
import java.util.UUID;

import com.github.unchama.seichiassist.commands.*;
import com.github.unchama.seichiassist.event.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.EffectData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class MinuteTaskRunnable extends BukkitRunnable{
	private SeichiAssist plugin = SeichiAssist.plugin;
	private HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap;
	private Config config = SeichiAssist.config;
	Sql sql = SeichiAssist.sql;
	public static int time = 0;

	//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
	public MinuteTaskRunnable() {

	}

	@Override
	public void run() {
		//playermap = SeichiAssist.playermap;
		//plugin = SeichiAssist.plugin;
		if(SeichiAssist.DEBUG){
			Util.sendEveryMessage("プレイヤーの１分間の処理を実行");
		}

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
				if(SeichiAssist.DEBUG){
					Util.sendEveryMessage(playerdata.name + "は不在により処理中止");
				}
				continue;
			}
			//プレイﾔｰが必ずオンラインと分かっている処理
			//プレイヤーを取得
			Player player = plugin.getServer().getPlayer(playerdata.uuid);

			//放置判定
			if(player.getLocation().equals(playerdata.loc)){
				playerdata.idletime ++;
				if(SeichiAssist.DEBUG){
					Util.sendEveryMessage(playerdata.name + "のidletime加算" + playerdata.idletime);
				}
			}else{
				playerdata.loc = player.getLocation();
				playerdata.idletime = 0;
				if(SeichiAssist.DEBUG){
					Util.sendEveryMessage(playerdata.name + "のidletimeリセット");
				}
			}

			//プレイヤー名を取得
			String name = Util.getName(player);
			//総整地量を更新(返り血で重み分け済みの1分間のブロック破壊量が返ってくる)
			int increase = playerdata.calcMineBlock(player);
			//Levelを設定(必ず総整地量更新後に実施！)
			playerdata.updataLevel(player);
			//activeskillpointを設定
			playerdata.activeskilldata.updataActiveSkillPoint(player,playerdata.level);
			//総プレイ時間更新
			playerdata.calcPlayTick(player);

			if(SeichiAssist.DEBUG){
				Util.sendEveryMessage(playerdata.name + "のランク処理完了");
			}

			//以下の3つ、必ずこの順番で実施！(after更新→setIncrease→before更新)
			//現在（after）の統計量を設定
			//playerdata.minuteblock.after = playerdata.totalbreaknum;
			//1分前(before)との差を計算し、設定
			//playerdata.minuteblock.setIncrease();
			//現在の統計量を設定(before)
			//playerdata.minuteblock.before = playerdata.totalbreaknum;


			//effectの大きさ
			double amplifier = 0;
			//effectのメッセージ
			//１分間のブロック破壊量による上昇
			amplifier = (double) increase * config.getMinuteMineSpeed();
			playerdata.effectdatalist.add(new EffectData(amplifier,2));

			//プレイヤー数による上昇
			amplifier = (double) onlinenums * config.getLoginPlayerMineSpeed();
			playerdata.effectdatalist.add(new EffectData(amplifier,1));


			//effect追加の処理
			//実際に適用されるeffect量
			int minespeedlv = 0;

			//effectflag ONの時のみ実行
			if (playerdata.effectflag != 5) {
				//合計effect量
				double sum = 0;
				//最大持続時間
				int maxduration = 0;
				//effectdatalistにある全てのeffectについて計算
				for(EffectData ed :playerdata.effectdatalist){
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
				if (playerdata.effectflag == 0) {
                    maxSpeed = 25565;
                } else if (playerdata.effectflag == 1) {
				    maxSpeed = 127;
				} else if(playerdata.effectflag == 2) {
					maxSpeed = 200;
				} else if(playerdata.effectflag == 3) {
					maxSpeed = 400;
				} else if(playerdata.effectflag == 4) {
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
				playerdata.minespeedlv = minespeedlv;
			}

			//プレイヤーにメッセージ送信
			if(playerdata.lastminespeedlv != minespeedlv || playerdata.messageflag){//前の上昇量と今の上昇量が違うか内訳表示フラグがオンの時告知する
				player.sendMessage(ChatColor.YELLOW + "★" + ChatColor.WHITE + "採掘速度上昇レベルが" + ChatColor.YELLOW + (minespeedlv+1) + ChatColor.WHITE +"になりました");
				if(playerdata.messageflag){
					player.sendMessage("----------------------------内訳-----------------------------");
					for(EffectData ed : playerdata.effectdatalist){
						player.sendMessage(ChatColor.RESET + "" +  ChatColor.RED + "" + ed.EDtoString(ed.id,ed.duration,ed.amplifier));
					}
					player.sendMessage("-------------------------------------------------------------");
				}
			}

			//プレイヤーデータを更新
			playerdata.lastminespeedlv = minespeedlv;

			if(SeichiAssist.DEBUG){
				Util.sendEveryMessage(playerdata.name + "のエフェクト処理が成功");
			}

			//ガチャ券付与の処理

			//ガチャポイントに合算
			playerdata.gachapoint += increase;

			if(playerdata.gachapoint >= config.getGachaPresentInterval() && playerdata.gachaflag){
				ItemStack skull = Util.getskull(name);
				playerdata.gachapoint -= config.getGachaPresentInterval();
				if(player.getInventory().contains(skull) || !Util.isPlayerInventryFill(player)){
					Util.addItem(player,skull);
					player.sendMessage(ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "プレゼントフォーユー。右クリックで使えるゾ");
				}else{
					Util.dropItem(player,skull);
					player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
					player.sendMessage(ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "がドロップしました。右クリックで使えるゾ");
				}
			}else{
				if(increase != 0 && playerdata.gachaflag){
					player.sendMessage("あと" + ChatColor.AQUA + (config.getGachaPresentInterval()-(playerdata.gachapoint % config.getGachaPresentInterval())) + ChatColor.WHITE + "ブロック整地すると" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "獲得ダヨ");
				}
			}
			//プレイヤーデータを更新
			playerdata.lastgachapoint = playerdata.gachapoint;

			if(SeichiAssist.DEBUG){
				Util.sendEveryMessage(playerdata.name + "のガチャ処理が成功");
			}




			//実績解除判定
			//実績解除処理部分の読みこみ
    		TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
    		//No1000系統の解禁チェック
    		int checkNo = 1001 ;
    		for(;checkNo < 1010 ;){
    			if(!playerdata.TitleFlags.get(checkNo)){
    				TUTR.TryTitle(player,checkNo);
    			}
    			checkNo ++ ;
    		}
    		//No3000系統の解禁チェック
    		checkNo = 3001 ;
    		for(;checkNo < 3012 ;){
    			if(!playerdata.TitleFlags.get(checkNo)){
    				TUTR.TryTitle(player,checkNo);
    			}
    			checkNo ++ ;
    		}
    		//No4000系統の解禁チェック
    		checkNo = 4001 ;
    		for(;checkNo < 4011 ;){
    			if(!playerdata.TitleFlags.get(checkNo)){
    				TUTR.TryTitle(player,checkNo);
    			}
    			checkNo ++ ;
    		}
    		//No5000系統の解禁チェック
    		checkNo = 5001 ;
    		for(;checkNo < 5009 ;){
    			if(!playerdata.TitleFlags.get(checkNo)){
    				TUTR.TryTitle(player,checkNo);
    			}
    			checkNo ++ ;
    		}
    		//No5100系統の解禁チェック
    		checkNo = 5101 ;
    		for(;checkNo < 5112 ;){
    			if(!playerdata.TitleFlags.get(checkNo)){
    				TUTR.TryTitle(player,checkNo);
    			}
    			checkNo ++ ;
    		}
    		//No6000系統の解禁チェック
    		checkNo = 6001 ;
    		for(;checkNo < 6009 ;){
    			if(!playerdata.TitleFlags.get(checkNo)){
    				TUTR.TryTitle(player,checkNo);
    			}
    			checkNo ++ ;
    		}
    		//No8000系統の解禁チェック
    		checkNo = 8001 ;
    		for(;checkNo < 8003 ;){
    			if(!playerdata.TitleFlags.get(checkNo)){
    				TUTR.TryTitle(player,checkNo);
    			}
    			checkNo ++ ;
    		}

    		//投票妖精関連
    		VotingFairyTaskRunnable VFTR = new VotingFairyTaskRunnable() ;

    		//マナ回復処理
    		if(playerdata.canVotingFairyUse == true){
    			VFTR.RecoveryMana(player);
    		}
    		//効果時間中か
    		if( playerdata.canVotingFairyUse == true && Util.isVotingFairyPeriod(playerdata.VotingFairyStartTime, playerdata.VotingFairyEndTime) == false ){
    			playerdata.canVotingFairyUse = false ;
    			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "時間だから、僕はこれで失礼するよ");
    			player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "妖精は何処かへ行ってしまったようだ...");
    			playerdata.hasVotingFairyMana = 0 ;
    		}

		}

        time++;
		GiganticFeverCommand.checkTime(); //GiganticFeverの時間チェック
	}
}
