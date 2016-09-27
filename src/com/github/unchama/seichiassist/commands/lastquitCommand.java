package com.github.unchama.seichiassist.commands;

import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.util.Util;

public class lastquitCommand implements TabExecutor{
	public SeichiAssist plugin;
	Sql sql = SeichiAssist.plugin.sql;


	public lastquitCommand(SeichiAssist plugin){
		this.plugin = plugin;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
	String label, String[] args) {

		//lastquit <Player> より多い引数を指定した場合
		if(args.length != 1){
			sender.sendMessage(ChatColor.RED + "/lastquit <プレイヤー名>");
			sender.sendMessage("該当プレイヤーの最終ログアウト日時を表示します");
			return true;
		}else{
			//プレイヤー名を取得
			String name = Util.getName(args[0]);

			sender.sendMessage(ChatColor.YELLOW + name + "の最終ログアウト日時を照会します…");

			//mysql
			String lastquit = sql.selectLastQuit(name);
			if(lastquit == null){
				sender.sendMessage(ChatColor.RED + "失敗");
			}else{
				sender.sendMessage(ChatColor.GREEN + "成功：" + lastquit);
			}
			return true;

		}
	}
}
