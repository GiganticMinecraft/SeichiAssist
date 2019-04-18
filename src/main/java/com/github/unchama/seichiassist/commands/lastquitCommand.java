package com.github.unchama.seichiassist.commands;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.util.Util;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

public class lastquitCommand implements TabExecutor{
	public SeichiAssist plugin;
	Sql sql = SeichiAssist.sql;


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
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "/lastquit <プレイヤー名>");
			sender.sendMessage("該当プレイヤーの最終ログアウト日時を表示します");
			return true;
		} else {
			//プレイヤー名を取得
			String name = Util.getName(args[0]);

			sender.sendMessage(ChatColor.YELLOW + name + "の最終ログアウト日時を照会します…");

			//mysql
			String lastquit = sql.selectLastQuit(name);
			if (lastquit.equals("")) {
				sender.sendMessage(ChatColor.RED + "失敗");
				sender.sendMessage(ChatColor.RED + "プレイヤー名やプレイヤー名が変更されていないか確認してください");
				sender.sendMessage(ChatColor.RED + "プレイヤー名が正しいのにこのエラーが出る場合、最終ログイン時間が古い可能性があります");
			} else {
				sender.sendMessage(ChatColor.GREEN + "成功：" + lastquit);
			}
			return true;

		}
	}
}
