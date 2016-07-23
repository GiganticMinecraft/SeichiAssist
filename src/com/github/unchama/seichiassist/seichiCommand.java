package com.github.unchama.seichiassist;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public class seichiCommand implements TabExecutor {
	SeichiAssist plugin;

	public seichiCommand(SeichiAssist _plugin){
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

		if(args.length == 0){
			Config.reloadConfig();
			sender.sendMessage("SeichiAssistのconfig.ymlをリロードしました。");
			return true;
		}
		return false;
	}

}
