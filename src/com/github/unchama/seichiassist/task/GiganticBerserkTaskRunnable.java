package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Mana;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class GiganticBerserkTaskRunnable {
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	Player player;
	PlayerData playerdata;

	public void PlayerKillEnemy(Player p){
		player = p;
		UUID uuid = p.getUniqueId();
		playerdata = playermap.get(uuid);
		Mana mana = playerdata.activeskilldata.mana;

		Random rnd = new Random();

		//確率でマナを回復させる
		double d = Math.random();
		if(d < 0.01*(playerdata.GBlevel + 1)){
			double i = 0,l = 0;
			switch(playerdata.GBstage){
			case 0:
				i = 100;
				break;
			case 1:
				i = 250;
				break;
			case 2:
				i = 500;
				break;
			case 3:
				i = 1500;
				break;
			case 4:
				i = 3000;
				break;
			default:
				break;
			}
			//元の値の20%を取り出してから元の値を10%マイナスし、取り出した値の振り幅でランダムに増加させる
			l = i/5;
			i -= l/2;
			i += rnd.nextInt((int)l + 1);

			mana.increaseMana(i,p,playerdata.level);
			player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Gigantic" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Berserk" + ChatColor.WHITE + "の効果でマナが" + i +"回復しました");
			player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1, (float)0.5) ;
		}

		//最大レベルの場合終了
		if(playerdata.GBstage == 4 && playerdata.GBlevel == 9){
			return;
		}

		//進化待機状態の場合終了
		if(playerdata.isGBStageUp){
			return;
		}


		int n = (playerdata.GBstage * 10) + playerdata.GBlevel;

		playerdata.GBexp ++;
		//レベルアップするかどうか判定
		if(SeichiAssist.GBlevellist.get(n) <= playerdata.GBexp){
			if(playerdata.GBlevel <= 8){
				playerdata.GBexp = 0;
				playerdata.GBlevel ++ ;
				//プレイヤーにメッセージ
				player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Gigantic" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Berserk" + ChatColor.WHITE + "のレベルがアップし、確率が上昇しました");
				player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)0.8) ;
				//最大レベルになった時の処理
				if(playerdata.GBstage == 4 && playerdata.GBlevel == 9){
					Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, (float)1.2);
					Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.name + "がパッシブスキル:" + ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Gigantic" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Berserk" + ChatColor.GOLD + "" + ChatColor.BOLD + "を完成させました！");
				}
			}
			//レベルが10かつ段階がダイヤ未満の場合は進化待機状態へ
			else if(playerdata.GBstage <= 3){
				player.sendMessage(ChatColor.GREEN + "パッシブスキルメニューより" + ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Gigantic" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Berserk" + ChatColor.GREEN + "スキルが進化可能です。");
				player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)0.8) ;
				playerdata.isGBStageUp = true;
			}
		}
	}
}