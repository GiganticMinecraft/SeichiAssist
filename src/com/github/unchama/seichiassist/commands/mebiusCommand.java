package com.github.unchama.seichiassist.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.listener.MebiusListener;

public class mebiusCommand implements TabExecutor {

	public mebiusCommand(SeichiAssist plugin) {
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.isOp() && args.length == 1 && args[0].equals("get")) {
			MebiusListener.debugGive((Player) sender);
		} else if (sender.isOp() && args.length == 1 && args[0].equals("reload")) {
			MebiusListener.reload();
		} else if (sender.isOp() && args.length == 1 && args[0].equals("debug")) {
			MebiusListener.debug((Player) sender);
		} else if (args.length == 2 && args[0].equals("naming")) {
			if (!MebiusListener.setName((Player) sender, args[1])) {
				sender.sendMessage("命名はMEBIUSを装備して行ってください。");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "[Usage]");
			sender.sendMessage(ChatColor.RED + "/mebius naming <name>");
			sender.sendMessage(ChatColor.RED + "  現在頭に装着中のMEBIUSに<name>を命名します。");
		}
		return true;
	}

}
