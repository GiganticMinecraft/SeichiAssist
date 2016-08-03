package com.github.unchama.seichiassist.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.Util;

public class effectCommand implements TabExecutor {
	SeichiAssist plugin;

	public effectCommand(SeichiAssist _plugin){
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
		Sql sql = SeichiAssist.plugin.sql;
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GREEN + "このコマンドはゲーム内から実行してください。");
			return true;
		}else if(args.length == 0){
			Player player = (Player)sender;
			String name = Util.getName(player);
			boolean effectflag = !sql.selectboolean(SeichiAssist.PLAYERDATA_TABLENAME,name, "effectflag");
			if (effectflag){
				sender.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON");
			}else{
				sender.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:OFF(ONに戻したい時は再度コマンドを実行します。)");
			}
			sql.insert(SeichiAssist.PLAYERDATA_TABLENAME,"effectflag", effectflag, name);
			return true;
		}else if(args.length == 1){
			if(args[0].equalsIgnoreCase("smart")){
				Player player = (Player)sender;
				String name = Util.getName(player);
				boolean messageflag = !sql.selectboolean(SeichiAssist.PLAYERDATA_TABLENAME,name, "messageflag");
				if (messageflag){
					sender.sendMessage(ChatColor.GREEN + "内訳表示:ON(OFFに戻したい時は再度コマンドを実行します。)");
				}else{
					sender.sendMessage(ChatColor.GREEN + "内訳表示:OFF");
				}
				sql.insert(SeichiAssist.PLAYERDATA_TABLENAME,"messageflag", messageflag, name);
				return true;
			}
		}
		return false;
	}

}
