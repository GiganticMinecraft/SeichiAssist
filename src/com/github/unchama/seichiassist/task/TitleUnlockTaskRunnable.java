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

		//以下予約配布システム用処理
		}else if(TryTitleNo >= 7001 && TryTitleNo <= 7999){
			//解禁時のフラグ変更処理
			playerdata.TitleFlags.set(TryTitleNo);
			player.sendMessage("【実績システム】運営チームよりNo" + TryTitleNo + "の二つ名がプレゼントされました。");



		}else {
		}
	}
}
