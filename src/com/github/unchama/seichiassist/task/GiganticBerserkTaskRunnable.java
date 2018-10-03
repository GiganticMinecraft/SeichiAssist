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

		//確率でマナを回復させる
		double d = Math.random();
		if(d < getProb(playerdata)){

			double i = getRecoveryValue(playerdata);

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


	/**
	 * マナ回復確率を返す
	 * @param playerdata
	 */
	public double getProb(PlayerData pd){
		if (pd.GBlevel < 2) return 0.05;
		else if (pd.GBlevel < 4) return 0.06;
		else if (pd.GBlevel < 6) return 0.07;
		else if (pd.GBlevel < 8) return 0.08;
		else if (pd.GBlevel < 9) return 0.09;
		else return 0.10;
	}

	public double getRecoveryValue(PlayerData playerdata){
		double i,l;
		Random rnd = new Random();

		switch (playerdata.GBstage){
		case 0:
			i = 300;
			switch (playerdata.GBlevel){
			case 0:
				l = 30;
				break;
			case 1:
				l = 35;
				break;
			case 2:
				l = 40;
				break;
			case 3:
				l = 45;
				break;
			case 4:
				l = 50;
				break;
			case 5:
				l = 60;
				break;
			case 6:
				l = 70;
				break;
			case 7:
				l = 80;
				break;
			case 8:
				l = 90;
				break;
			case 9:
				l = 100;
				break;
			default:
				l = 0;
			}
			break;
		case 1:
			i = 2000;
			switch (playerdata.GBlevel){
			case 0:
				l = 200;
				break;
			case 1:
				l = 220;
				break;
			case 2:
				l = 250;
				break;
			case 3:
				l = 270;
				break;
			case 4:
				l = 300;
				break;
			case 5:
				l = 350;
				break;
			case 6:
				l = 400;
				break;
			case 7:
				l = 450;
				break;
			case 8:
				l = 500;
				break;
			case 9:
				l = 600;
				break;
			default:
				l = 0;
			}
			break;
		case 2:
			i = 15000;
			switch (playerdata.GBlevel){
			case 0:
				l = 1500;
				break;
			case 1:
				l = 1650;
				break;
			case 2:
				l = 1800;
				break;
			case 3:
				l = 2000;
				break;
			case 4:
				l = 2200;
				break;
			case 5:
				l = 2400;
				break;
			case 6:
				l = 2600;
				break;
			case 7:
				l = 2800;
				break;
			case 8:
				l = 3000;
				break;
			case 9:
				l = 3200;
				break;
			default:
				l = 0;
			}
			break;
		case 3:
			i = 40000;
			switch (playerdata.GBlevel){
			case 0:
				l = 4000;
				break;
			case 1:
				l = 4400;
				break;
			case 2:
				l = 4800;
				break;
			case 3:
				l = 5200;
				break;
			case 4:
				l = 5600;
				break;
			case 5:
				l = 6000;
				break;
			case 6:
				l = 6500;
				break;
			case 7:
				l = 7000;
				break;
			case 8:
				l = 7500;
				break;
			case 9:
				l = 8000;
				break;
			default:
				l = 0;
			}
			break;
		case 4:
			i = 100000;
			switch (playerdata.GBlevel){
			case 0:
				l = 10000;
				break;
			case 1:
				l = 11000;
				break;
			case 2:
				l = 12000;
				break;
			case 3:
				l = 13000;
				break;
			case 4:
				l = 14000;
				break;
			case 5:
				l = 15000;
				break;
			case 6:
				l = 16000;
				break;
			case 7:
				l = 17000;
				break;
			case 8:
				l = 18500;
				break;
			case 9:
				l = 20000;
				break;
			default:
				l = 0;
			}
			break;
		default:
			i = 0;
			l = 0;
		}

		i -= i/10;
		i += rnd.nextInt((int)l + 1);

		return i;
	}
}