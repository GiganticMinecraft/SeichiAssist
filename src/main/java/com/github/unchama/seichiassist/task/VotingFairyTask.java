package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.listener.VotingFairyListener;
import com.github.unchama.seichiassist.util.Util;


public class VotingFairyTask {

	//MinuteTaskRunnableから、妖精召喚中のプレイヤーを対象に毎分実行される
	public static void run(Player p) {
		HashMap<UUID,PlayerData> playermap = SeichiAssist.Companion.getPlayermap();
		UUID uuid = p.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		//マナ回復
		VotingFairyListener.regeneMana(p);

		//効果時間中か
		if(!Util.isVotingFairyPeriod(playerdata.getVotingFairyStartTime(), playerdata.getVotingFairyEndTime())) {
			speak(p, ("あっ、もうこんな時間だ！"), false);
			speak(p, ("じゃーねー！" + p.getName()), true);
			p.sendMessage(ChatColor.RESET + "" + ChatColor.YELLOW + "" + ChatColor.BOLD + "妖精はどこかへ行ってしまった");
			playerdata.setUsingVotingFairy(false);
		}
	}

	public static void speak(Player p, String msg, boolean b) {
		if (b) playSe(p);
		p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "<マナ妖精>" + ChatColor.RESET + "" + msg);
	}

	//妖精効果音
	public static void playSe(Player p) {
		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 2f, 1f);
		Bukkit.getServer().getScheduler().runTaskLater(SeichiAssist.Companion.getInstance(), () -> {
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 2f, 1.5f);
			Bukkit.getServer().getScheduler().runTaskLater(SeichiAssist.Companion.getInstance(), () -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 2f, 2f), 2);
		}, 2);
	}

	public static String dispToggleVFTime(int toggle){

		return toggle == 1 ? "30分"
		  : toggle == 2 ? "1時間"
		  : toggle == 3 ? "1時間30分"
		  : toggle == 4 ? "2時間"
				  : "エラー";
	}
}
