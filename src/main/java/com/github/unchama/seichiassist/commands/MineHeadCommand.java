package com.github.unchama.seichiassist.commands;

import com.github.unchama.seichiassist.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MineHeadCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Player p = (Player) sender;
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GREEN + "このコマンドはゲーム内から実行してください。");
			return true;
		}else if(args.length == 0){
			Util.addItemToPlayerSafely(p, Util.getMineHeadItem());
			p.sendMessage(ChatColor.GREEN + "専用アイテムを付与しました．");
			return true;
		}
		return false;
	}
}