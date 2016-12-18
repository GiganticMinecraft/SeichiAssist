package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

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
		}else {

		}



	}
}