package com.github.unchama.seichiassist.task;

import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
//import org.bukkit.metadata.FixedMetadataValue;
//import org.bukkit.plugin.java.JavaPlugin;

public class TitleUnlockTaskRunnable {
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	Player player;
	PlayerData playerdata;
	int TryTitleNo;

//    private JavaPlugin plugin;

//	public void BlockLineUp(JavaPlugin plugin) {
//		this.plugin = plugin;
//		plugin.getServer().getPluginManager().registerEvents(this, plugin);
//	}



	//ここで処理対象のユーザーと、そのtitleNoを拾って処理を行う。
	public void TryTitle(Player p ,int i){
		player = p;
		UUID uuid = p.getUniqueId();
		playerdata = playermap.get(uuid);

		TryTitleNo = i ;

		//投げられたTitleNoごとにあてはまる解除判定を実行

		//整地ランキング(No1000系統)
		if(TryTitleNo == 1001){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.calcPlayerRank(player) == 1){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No1001解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 1002){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.calcPlayerRank(player) < 6){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No1002解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 1003){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.calcPlayerRank(player) < 28){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No1003解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 1004){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.calcPlayerRank(player) < 51){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No1004解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 1005){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.calcPlayerRank(player) < 101){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No1005解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 1006){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.calcPlayerRank(player) < 251){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No1006解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 1007){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.calcPlayerRank(player) < 501){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No1007解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 1008){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.calcPlayerRank(player) < 1001){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No1008解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 1009){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.calcPlayerRank(player) < 3001){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No1009解除！おめでとうございます！");
				}
			}
		//整地量(No3000系統)
		}else if(TryTitleNo == 3001){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.totalbreaknum > 2147483646 || playerdata.totalbreaknum < 0){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No3001解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 3002){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.totalbreaknum > 1000000000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No3002解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 3003){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.totalbreaknum > 500000000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No3003解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 3004){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.totalbreaknum > 100000000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No3004解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 3005){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.totalbreaknum > 50000000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No3005解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 3006){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.totalbreaknum > 10000000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No3006解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 3007){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.totalbreaknum > 5000000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No3007解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 3008){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.totalbreaknum > 1000000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No3008解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 3009){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.totalbreaknum > 500000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No3009解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 3010){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.totalbreaknum > 100000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No3010解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 3011){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.totalbreaknum > 10000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No3011解除！おめでとうございます！");
				}
			}
		//参加時間(No4000系統)
		}else if(TryTitleNo == 4001){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.playtick > 172800000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No4001解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 4002){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.playtick > 72000000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No4002解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 4003){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.playtick > 36000000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No4003解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 4004){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.playtick > 18000000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No4004解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 4005){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.playtick > 7200000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No4005解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 4006){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.playtick > 3600000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No4006解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 4007){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.playtick > 1728000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No4007解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 4008){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.playtick > 720000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No4008解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 4009){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.playtick > 360000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No4009解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 4010){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.playtick > 72000){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No4010解除！おめでとうございます！");
				}
			}

		//連続ログイン(No5000系統)
		}else if(TryTitleNo == 5001){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.ChainJoin >= 100){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5001解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5002){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.ChainJoin >= 50){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5002解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5003){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.ChainJoin >= 30){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5003解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5004){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.ChainJoin >= 20){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5004解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5005){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.ChainJoin >= 10){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5005解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5006){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.ChainJoin >= 5){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5006解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5007){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.ChainJoin >= 3){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5007解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5008){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.ChainJoin >= 2){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5008解除！おめでとうございます！");
				}
			}

		//通算ログイン(No5100系統)
		}else if(TryTitleNo == 5101){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.TotalJoin >= 365){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5101解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5102){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.TotalJoin >= 300){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5102解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5103){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.TotalJoin >= 200){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5103解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5104){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.TotalJoin >= 100){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5104解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5105){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.TotalJoin >= 75){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5105解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5106){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.TotalJoin >= 50){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5106解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5107){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.TotalJoin >= 30){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5107解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5108){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.TotalJoin >= 20){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5108解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5109){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.TotalJoin >= 10){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5109解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5110){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.TotalJoin >= 5){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5110解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 5111){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.TotalJoin >= 2){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No5111解除！おめでとうございます！");
				}
			}

		//投票数(No6000系統)
		}else if(TryTitleNo == 6001){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.p_vote_forT > 364){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No6001解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 6002){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.p_vote_forT > 199){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No6002解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 6003){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.p_vote_forT > 99){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No6003解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 6004){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.p_vote_forT > 49){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No6004解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 6005){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.p_vote_forT > 24){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No6005解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 6006){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.p_vote_forT > 9){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No6006解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 6007){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.p_vote_forT > 4){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No6007解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 6008){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.p_vote_forT > 0){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No6008解除！おめでとうございます！");
				}
			}

		//隠し実績(No8000系統)
		}else if(TryTitleNo == 8001){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				int flag8001 = 0 ;
				ItemStack ItemInInv ;

				for(; flag8001 < 36 ;){
					//該当スロットのアイテムデータ取得
					ItemInInv = player.getInventory().getItem(flag8001) ;
					if(ItemInInv == null ){
						flag8001 = 40 ;
					}else {
						if(!ItemInInv.getType().equals(Material.SKULL_ITEM)){
							flag8001 = 40 ;
						}else {
							SkullMeta skullmeta = (SkullMeta) ItemInInv.getItemMeta();

							if(!skullmeta.hasOwner()){
								flag8001 = 40;
							}
							else if(!skullmeta.getOwner().equals("unchama")){
								flag8001 = 40 ;
							}
							else {
								flag8001 ++ ;
							}
						}
					}
				}
				if(flag8001 == 36){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("【極秘任務】実績No8001解除！おめでとうございます！");
				}
			}
		}else if(TryTitleNo == 8002){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(playerdata.totalbreaknum % 1000000 == 777777){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("【極秘任務】実績No8002解除！おめでとうございます！");
				}
			}

		//特殊解禁(No9000系統)

		//日付解禁
		}else if(TryTitleNo == 9001){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 1 &&
					Calendar.getInstance().get(Calendar.DATE) == 1){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9001解除！あけおめ！");
				}else{
					player.sendMessage("実績No9001は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9002){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 12 &&
						Calendar.getInstance().get(Calendar.DATE) == 25){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9002解除！めりくり！");
				}else{
					player.sendMessage("実績No9002は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9003){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 12 &&
						Calendar.getInstance().get(Calendar.DATE) == 31){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9003解除！よいお年を！");
				}else{
					player.sendMessage("実績No9003は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9004){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 1 ){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9004解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9004は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9005){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 2 ){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9005解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9005は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9006){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 2 &&
					Calendar.getInstance().get(Calendar.DATE) == 3){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9006解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9006は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9007){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 2 &&
					Calendar.getInstance().get(Calendar.DATE) == 11){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9007解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9007は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9008){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 2 &&
					Calendar.getInstance().get(Calendar.DATE) == 14){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9008解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9008は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9009){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 3 ){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9009解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9009は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9010){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 3 &&
					Calendar.getInstance().get(Calendar.DATE) == 3){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9010解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9010は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9011){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 3 &&
					Calendar.getInstance().get(Calendar.DATE) == 14){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9011解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9011は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9012){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 3 &&
					Calendar.getInstance().get(Calendar.DATE) == 20){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9012解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9012は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9013){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 4 ){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9013解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9013は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9014){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 4 &&
					Calendar.getInstance().get(Calendar.DATE) == 1){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9014解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9014は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9015){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 4 &&
					Calendar.getInstance().get(Calendar.DATE) == 15){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9015解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9015は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9016){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 4 &&
					Calendar.getInstance().get(Calendar.DATE) == 22){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9016解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9016は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9017){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 5 ){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9017解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9017は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9018){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 5 &&
					Calendar.getInstance().get(Calendar.DATE) == 5){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9018解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9018は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9019){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 5 &&
					Calendar.getInstance().get(Calendar.DATE) == 5){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9019解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9019は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9020){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 5 &&
					Calendar.getInstance().get(Calendar.DATE) == 14){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9020解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9020は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9021){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 6 ){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9021解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9021は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9022){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 6 &&
					Calendar.getInstance().get(Calendar.DATE) == 12){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9022解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9022は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9023){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 6 &&
					Calendar.getInstance().get(Calendar.DATE) == 17){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9023解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9023は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9024){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 6 &&
					Calendar.getInstance().get(Calendar.DATE) == 29){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9024解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9024は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9025){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 7 ){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9025解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9025は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9026){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 7 &&
					Calendar.getInstance().get(Calendar.DATE) == 7){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9026解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9026は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9027){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 7 &&
					Calendar.getInstance().get(Calendar.DATE) == 17){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9027解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9027は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9028){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 7 &&
					Calendar.getInstance().get(Calendar.DATE) == 29){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9028解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9028は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9029){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 8 ){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9029解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9029は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9030){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 8 &&
					Calendar.getInstance().get(Calendar.DATE) == 7){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9030解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9030は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9031){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 8 &&
					Calendar.getInstance().get(Calendar.DATE) == 16){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9031解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9031は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9032){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 8 &&
					Calendar.getInstance().get(Calendar.DATE) == 29){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9032解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9032は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9033){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 9 ){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9033解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9033は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9034){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 9 &&
					Calendar.getInstance().get(Calendar.DATE) == 2){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9034解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9034は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9035){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 9 &&
					Calendar.getInstance().get(Calendar.DATE) == 12){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9035解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9035は条件を満たしていません。");
				}
			}
		}else if(TryTitleNo == 9036){
			if(!playerdata.TitleFlags.get(TryTitleNo)){
				//解除処理
				if(Calendar.getInstance().get(Calendar.MONTH) + 1 == 9 &&
					Calendar.getInstance().get(Calendar.DATE) == 29){
				//解禁時のフラグ変更処理
				playerdata.TitleFlags.set(TryTitleNo);
				player.sendMessage("実績No9036解除！おめでとうございます！");
				}else{
					player.sendMessage("実績No9036は条件を満たしていません。");
				}
			}

		//以下予約配布システム用処理
		}else if(TryTitleNo >= 7001 && TryTitleNo <= 7999){
			//解禁時のフラグ変更処理
			playerdata.TitleFlags.set(TryTitleNo);
			player.sendMessage("【実績システム】運営チームよりNo" + TryTitleNo + "の二つ名がプレゼントされました。");



		}else {
		}
	}
}
