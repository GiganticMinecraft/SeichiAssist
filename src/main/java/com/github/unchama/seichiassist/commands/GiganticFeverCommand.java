package com.github.unchama.seichiassist.commands;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.task.MinuteTaskRunnable;
import com.github.unchama.seichiassist.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by karayuu on 2018/07/25
 */
public class GiganticFeverCommand implements CommandExecutor {
	private Config config = SeichiAssist.config;
	private static int end = 0;
	private static boolean isInTime = false;

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		int now = MinuteTaskRunnable.time;
		end = now + config.getGiganticFeverMinutes();
		isInTime = true;

		Util.sendEveryMessage(ChatColor.AQUA + "フィーバー！この時間MOBたちは踊りに出かけてるぞ！今が整地時だ！");
		Util.sendEveryMessage(ChatColor.AQUA + "(" + config.getGiganticFeverDisplayTime() + "間)");

		Util.setDifficulty(SeichiAssist.seichiWorldList, Difficulty.PEACEFUL);

		return true;
	}

	public static void checkTime() {
		if (!isInTime) {
			return;
		}
		if (MinuteTaskRunnable.time == end) {
			Util.setDifficulty(SeichiAssist.seichiWorldList, Difficulty.HARD);
			Util.sendEveryMessage(ChatColor.AQUA + "フィーバー終了！MOBたちは戻ってきたぞ！");
			isInTime = false;
		}
	}

}
