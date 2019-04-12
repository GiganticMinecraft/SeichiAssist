package com.github.unchama.seichiassist.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class contributeCommand implements TabExecutor {
	SeichiAssist plugin;

	public contributeCommand(SeichiAssist _plugin){
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


		Sql sql = SeichiAssist.sql;


		//受け取るプレイヤーの情報を取得
		Player givenplayer = Bukkit.getServer().getPlayer(args[1]);

		if(args.length == 0){
			//コマンド長が0の時の処理
			sender.sendMessage(ChatColor.GREEN + "/contribute <add/remove> <playername> <point>");
			return true;

		}else if(args[0].equalsIgnoreCase("add") && args.length == 3){

			//sqlをusernameで操作
			if (sql.setContribute(sender, args[1], Util.toInt(args[2]))) {
				sender.sendMessage(ChatColor.GREEN + args[1] + "に貢献度ポイント" + args[2] + "を追加しました");

				//指定プレイヤーがオンラインの場合即時反映
				if (givenplayer != null) {
					//UUIDを取得
					UUID givenuuid = givenplayer.getUniqueId();
					//playerdataを取得
					PlayerData givenplayerdata = SeichiAssist.playermap.get(givenuuid);

					//splを直接書き換えているのでplayerdataを同じ数値だけ変化させてから計算させる
					givenplayerdata.contribute_point += Util.toInt(args[2]);

					givenplayerdata.isContribute(givenplayer, Util.toInt(args[2]));
				}
			}
			return true;
		}else if(args[0].equalsIgnoreCase("remove") && args.length == 3){

			//sqlをusernameで操作
			if (sql.setContribute(sender, args[1], (-1 * Util.toInt(args[2])))) {
				sender.sendMessage(ChatColor.GREEN + args[1] + "の貢献度ポイントを" + args[2] + "減少させました");

				//指定プレイヤーがオンラインの場合即時反映
				if (givenplayer != null) {
					//UUIDを取得
					UUID givenuuid = givenplayer.getUniqueId();
					//playerdataを取得
					PlayerData givenplayerdata = SeichiAssist.playermap.get(givenuuid);

					//splを直接書き換えているのでplayerdataを同じ数値だけ変化させてから計算させる
					givenplayerdata.contribute_point += (-1 * Util.toInt(args[2]));

					givenplayerdata.isContribute(givenplayer, (-1 * Util.toInt(args[2])));
				}
				return true;
			}
		}else if(args[0].equalsIgnoreCase("help")){
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD +"[コマンドリファレンス]");
			sender.sendMessage(ChatColor.RED + "/contribute add <プレイヤー名> <増加分ポイント>");
			sender.sendMessage("指定されたプレイヤーの貢献度ptを指定分増加させます");

			sender.sendMessage(ChatColor.RED + "/contribute remove <プレイヤー名> <減少分ポイント>");
			sender.sendMessage("指定されたプレイヤーの貢献度ptを指定分減少させます(入力ミス回避用)");

			return true;
		}else if(args[0].equalsIgnoreCase("add")){
			sender.sendMessage(ChatColor.GREEN + "/contribute add <playername> <point>");

			return true;
		}else if(args[0].equalsIgnoreCase("remove")){
			sender.sendMessage(ChatColor.GREEN + "/contribute remove <playername> <point>");

			return true;
		}else if(args.length == 2){
			//コマンド長が2の時の処理
			sender.sendMessage(ChatColor.GREEN + "/contribute <add/remove> <playername> <point>");

			return true;
		}
		return false;
	}
}
