package com.github.unchama.seichiassist.commands;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.seichiassist.util.TypeConverter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ContributeCommand implements CommandExecutor {

	// TODO 各ブランチに分けられるべき
	private static void printUsageTo(CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "/contribute <add/remove> <playername> <point>");
	}

	// TODO 各ブランチに分けられるべき
	private static void printHelpTo(CommandSender sender) {
		sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD +"[コマンドリファレンス]");
		sender.sendMessage(ChatColor.RED + "/contribute add <プレイヤー名> <増加分ポイント>");
		sender.sendMessage("指定されたプレイヤーの貢献度ptを指定分増加させます");
		sender.sendMessage(ChatColor.RED + "/contribute remove <プレイヤー名> <減少分ポイント>");
		sender.sendMessage("指定されたプレイヤーの貢献度ptを指定分減少させます(入力ミス回避用)");
	}

	// TODO 各ブランチに分けられるべき
	private static void addContributionPoint(CommandSender sender, String targetPlayerName, int point) {
		final DatabaseGateway databaseGateway = SeichiAssist.databaseGateway;

		//sqlをusernameで操作
		if (databaseGateway.playerDataManipulator.addContributionPoint(sender, targetPlayerName, point)) {
			sender.sendMessage(point >= 0
					? ChatColor.GREEN + targetPlayerName + "に貢献度ポイント" + point + "を追加しました"
					: ChatColor.GREEN + targetPlayerName + "の貢献度ポイントを" + point + "減少させました");

			@Nullable Player targetPlayer = Bukkit.getServer().getPlayer(targetPlayerName);

			//指定プレイヤーがオンラインの場合即時反映
			if (targetPlayer != null) {
				PlayerData targetPlayerData = SeichiAssist.playermap.get(targetPlayer.getUniqueId());

				// DBのデータを直接書き換えているのでplayerdataを同じ数値だけ変化させてから計算させる
				targetPlayerData.contribute_point += point;
				targetPlayerData.isContribute(targetPlayer, point);
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length < 3) {
			printUsageTo(sender);
			return true;
		}

		final String firstArg = args[0];
		final String targetPlayerName = args[1];
		final Integer point = TypeConverter.toIntSafe(args[2]);

		if (point == null) {
			printUsageTo(sender);
			return true;
		}

		if(firstArg.equalsIgnoreCase("add")) {
			addContributionPoint(sender, targetPlayerName, point);
			return true;
		} else if (firstArg.equalsIgnoreCase("remove")) {
			addContributionPoint(sender, targetPlayerName, -point);
			return true;
		} else if(firstArg.equalsIgnoreCase("help")) {
			printHelpTo(sender);
			return true;
		}

		return false;
	}
}
