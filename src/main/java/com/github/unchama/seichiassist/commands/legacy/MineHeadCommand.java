package com.github.unchama.seichiassist.commands.legacy;

import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory;
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
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GREEN + "このコマンドはゲーム内から実行してください。");
			return true;
		}else if(args.length == 0){
			Player p = (Player) sender;
			Util.addItemToPlayerSafely(p, StaticGachaPrizeFactory.getMineHeadItem());
			p.sendMessage(ChatColor.GREEN + "専用アイテムを付与しました．");
			return true;
		}
		return false;
	}
}