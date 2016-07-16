package com.github.unchama.multiseichieffect;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public class seichiCommand implements TabExecutor {
	private MultiSeichiEffect plugin;

	public seichiCommand(MultiSeichiEffect _plugin){
		plugin = _plugin;
	}
	@Override
	public List<String> onTabComplete(CommandSender arg0, Command arg1,
			String arg2, String[] arg3) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {

		if(args.length > 1){
			sender.sendMessage("引数は1つにまでにして下さい");
			return true;

		}else if(args.length == 1){
			if(args[0].equalsIgnoreCase("reload")){
				plugin.reloadConfig();
				sender.sendMessage("reload completed");
				return true;
			}
		}
		return false;
	}

}
