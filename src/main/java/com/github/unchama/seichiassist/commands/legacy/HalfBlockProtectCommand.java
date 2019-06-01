package com.github.unchama.seichiassist.commands.legacy;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HalfBlockProtectCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		if (!(commandSender instanceof Player)) {
			commandSender.sendMessage(ChatColor.RED + "このコマンドはゲーム内から実行してください.");
			return true;
		}

		if (strings.length == 0) {
			Player p = (Player) commandSender;
			UUID uuid = p.getUniqueId();
			PlayerData data = SeichiAssist.playermap.get(uuid);

			data.toggleHalfBreakFlag();
			p.sendMessage("現在ハーフブロックは" + getStats(data.canBreakHalfBlock()) + ChatColor.RESET + "です.");
			return true;
		}
		return false;
	}

	private String getStats (boolean canBreak) {
		if (canBreak) {
			return ChatColor.GREEN + "破壊可能";
		} else {
			return ChatColor.RED + "破壊不可能";
		}
	}

}
