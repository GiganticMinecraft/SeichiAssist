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

public class VotingFairyTaskRunnable {
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	Player player;
	PlayerData playerdata;

	public void SummonFairy(Player p){
		player = p;
		UUID uuid = p.getUniqueId();
		playerdata = playermap.get(uuid);

		Random rnd = new Random();

		playerdata.hasVotingFairyMana = 0 ;

		playerdata.activeskilldata.effectpoint -= 10;
		playerdata.canVotingFairyUse = true ;

		//マナ回復量を決定
		/*調整前数値
		//プレイヤーが90レベル未満のとき
		if(playerdata.level < 90){
			playerdata.VotingFairyRecoveryValue = rnd.nextInt(501) + 100;
		}
		//175未満のとき
		else if(playerdata.level < 175){
			playerdata.VotingFairyRecoveryValue = rnd.nextInt(1201) + 400;
		}
		else{
			playerdata.VotingFairyRecoveryValue = rnd.nextInt(6001) + 2000;
		}
		*/
		//調整後
		if(playerdata.ChainVote >= 5){
			if(playerdata.level < 80){
				playerdata.VotingFairyRecoveryValue = rnd.nextInt(401) + 200;
			}
			else if(playerdata.level < 110){
				playerdata.VotingFairyRecoveryValue = rnd.nextInt(751) + 500;
			}
			else if(playerdata.level < 140){
				playerdata.VotingFairyRecoveryValue = rnd.nextInt(1501) + 1000;
			}
			else if(playerdata.level < 180){
				playerdata.VotingFairyRecoveryValue = rnd.nextInt(3001) + 2000;
			}
			else {
				playerdata.VotingFairyRecoveryValue = rnd.nextInt(3001) + 5000;
			}
		}else {
			if(playerdata.level < 80){
				playerdata.VotingFairyRecoveryValue = rnd.nextInt(501) + 100;
			}
			else if(playerdata.level < 110){
				playerdata.VotingFairyRecoveryValue = rnd.nextInt(1001) + 250;
			}
			else if(playerdata.level < 140){
				playerdata.VotingFairyRecoveryValue = rnd.nextInt(2001) + 500;
			}
			else if(playerdata.level < 180){
				playerdata.VotingFairyRecoveryValue = rnd.nextInt(4001) + 1000;
			}
			else {
				playerdata.VotingFairyRecoveryValue = rnd.nextInt(5501) + 2500;
			}
		}

		//プレイヤーにメッセージ
		player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "やあ、" + player.getName() + "。僕を呼んだのは君だね?" );
		player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "投票ptメニューからガチャりんごを渡してくれれば" );
		player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "マナを少しずつ回復してあげるよ" );

		if(playerdata.ChainJoin >= 30){
			playerdata.VotingFairyRecoveryValue += 500;
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "そうだねぇ…君は" + playerdata.ChainJoin + "日も連続でログインしてくれてるから…");
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "多めにおまけして1分間に" + playerdata.VotingFairyRecoveryValue + "マナにしてあげよう");
		}
		else if(playerdata.ChainJoin >= 10){
			playerdata.VotingFairyRecoveryValue += 250;
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "そうだねぇ…君は" + playerdata.ChainJoin + "日連続でログインしてくれてるから…");
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "おまけして1分間に" + playerdata.VotingFairyRecoveryValue + "マナにしてあげよう");
		}
		else if(playerdata.ChainJoin >= 5){
			playerdata.VotingFairyRecoveryValue += 100;
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "そうだねぇ…君は" + playerdata.ChainJoin + "日連続でログインしてくれてるから…");
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "ちょっとおまけして1分間に" + playerdata.VotingFairyRecoveryValue + "マナにしてあげよう");
		}
		else {
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "そうだねぇ…1分間に" + playerdata.VotingFairyRecoveryValue + "マナくらいかな" );
		}
	}

	public void GiveApple(Player p){
		player = p;
		UUID uuid = p.getUniqueId();
		playerdata = playermap.get(uuid);

		//MineStack番号379(がちゃりんご)の所有数で分ける
		if(playerdata.giveApple <= playerdata.minestack.getNum(379)){

			double n = 1.0;

			if(playerdata.ChainVote >= 10){
				n = 1.5;
			}
			else if(playerdata.ChainVote >= 2){
				n = 1.2;
			}

			playerdata.hasVotingFairyMana += playerdata.giveApple * 300 * n;

			playerdata.minestack.setNum(379, playerdata.minestack.getNum(379) - playerdata.giveApple);
			player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, (float)1.2) ;

			//プレイヤーにメッセージ
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "ガチャりんご" + playerdata.giveApple + "個、君のMineStackから確かに受け取ったよ" );

			if(playerdata.ChainVote >= 10){
				player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "君は" + playerdata.ChainVote + "日も連続で投票してくれてるから、奮発してあげよう" );
			}
			else if(playerdata.ChainVote >= 2){
				player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "君は" + playerdata.ChainVote + "日連続で投票してくれてるから、ちょっと奮発してあげよう" );
			}

		}
		else{
			player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1) ;

			//プレイヤーにメッセージ
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "ガチャりんご" + playerdata.giveApple + "個、君のMineStackから…" );
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "おいおい、君はそんなにガチャりんごを持っていないだろう" );
		}

		playerdata.giveApple = 0;
	}

	public void RecoveryMana(Player p){
		player = p;
		UUID uuid = p.getUniqueId();
		playerdata = playermap.get(uuid);
		Mana mana = playerdata.activeskilldata.mana;

		if(playerdata.hasVotingFairyMana <= 0){

			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "ガチャりんごがないと君のマナを回復できないよ" );
			return;
		}
		else if(playerdata.hasVotingFairyMana > playerdata.VotingFairyRecoveryValue){
			mana.increaseMana(playerdata.VotingFairyRecoveryValue, p, playerdata.level);
			playerdata.hasVotingFairyMana -= playerdata.VotingFairyRecoveryValue ;

			//プレイヤーにメッセージ
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "(´～｀)ﾓｸﾞﾓｸﾞ…" );
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "君のマナを" + playerdata.VotingFairyRecoveryValue + "回復させておいたよ");
		}
		else if(playerdata.hasVotingFairyMana <= playerdata.VotingFairyRecoveryValue){
			mana.increaseMana(playerdata.hasVotingFairyMana, p, playerdata.level);

			//プレイヤーにメッセージ
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "(´～｀)ﾓｸﾞﾓｸﾞ…" );
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "君のマナを" + playerdata.hasVotingFairyMana + "回復させておいたよ");
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "貰ったガチャりんごがなくなっちゃった…" );
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "棒メニューからまた渡してくれると嬉しいな" );


			playerdata.hasVotingFairyMana -= playerdata.hasVotingFairyMana ;
		}
	}

	public void askApple(Player p){
		player = p;
		UUID uuid = p.getUniqueId();
		playerdata = playermap.get(uuid);
		int n = 300;

		if(playerdata.ChainVote >= 10){
			n *= 1.5;
		}
		else if(playerdata.ChainVote >= 2){
			n *= 1.2;
		}

		if(playerdata.hasVotingFairyMana >= 1000){
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "君に貰ったりんごがどのくらい残ってるかって…?");
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "そうだねぇ…大体" + playerdata.hasVotingFairyMana/n + "個くらいかな");
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "もっともらえると嬉しいなぁ…(´▽｀*)");
			player.closeInventory();
		}
		else if(playerdata.hasVotingFairyMana == 0){
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "君に貰ったりんごがどのくらい残ってるかって…?");
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "冗談言うなよ、一つも残ってないさ");
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "はやくもらえると嬉しいなぁ…(´▽｀*)");
			player.closeInventory();
		}
		else{
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "君に貰ったりんごがどのくらい残ってるかって…?");
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "そうだねぇ…もうすぐなくなりそうかな");
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "もっともらえると嬉しいなぁ…(´▽｀*)");
			player.closeInventory();
		}
	}
}
