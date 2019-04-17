package com.github.unchama.seichiassist.commands;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author unicroak
 * TODO: Implement CommandExecutor instead of TabExecutor
 */
public final class AchieveCommand implements TabExecutor {

	private final Map<UUID, PlayerData> playerDataMap = SeichiAssist.playermap;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		final Sql sql = SeichiAssist.sql;
		final boolean executedByPlayer = sender instanceof Player;

		if (!executedByPlayer) {
			sender.sendMessage("コンソールからのunlockachv処理実行を検知しました。");
			sender.sendMessage("コンソール実行の場合はオフラインユーザーへの配布、「world」処理は実行できません。");
		}

		if (args.length != 2 && args.length != 3) {
			sender.sendMessage(ChatColor.RED + "/unlockachv <実績No> <プレイヤー名> <give/deprive>");
			sender.sendMessage("【HINT】<プレイヤー名>を「ALL」にし、<give/deprive>の代わりに<server/world>を入力すると");
			sender.sendMessage("実行者が参加しているサーバー/ワールド内の全員に対して実績解除処理を実行します。");

			return true;
		}

		final String target = args[1];
		final String option = args.length == 3 ? args[2] : "";

		final Optional<Integer> achievementNumberOptional = tryToParseInt(args[0]);
		if (!achievementNumberOptional.isPresent()) {
			sender.sendMessage("【実行エラー】実績Noの項目は半角数字で入力してください");
			sender.sendMessage(ChatColor.RED + "/unlockachv <実績No> <プレイヤー名> <give/deprive>");

			return true;
		}
		final int achievementNumber = achievementNumberOptional.get();

		if (!(1000 <= achievementNumber && achievementNumber < 10000)) {
			sender.sendMessage("【実行エラー】解禁コマンドが使用できるのはNo1000～9999の実績です。");
			sender.sendMessage(ChatColor.RED + "/unlockachv <実績No> <プレイヤー名> <give/deprive>");

			return true;
		}

		if (existsInConfig(achievementNumber)) {
			sender.sendMessage("【実行エラー】存在しない実績Noが指定されました");

			return true;
		}

		if (target.equalsIgnoreCase("all")) {
			switch (option.toUpperCase()) {
				case "SERVER":
					Bukkit.getServer().getOnlinePlayers()
							.forEach(player -> giveAchievement(player, achievementNumber));
					sender.sendMessage("【配布完了】No" + args[0] + "の実績をサーバー内全員に配布しました。");

					return true;
				case "WORLD":
					if (!executedByPlayer) {
						sender.sendMessage("コンソール実行の場合は「world」処理は実行できません。");
					} else {
						final Player sendPlayer = (Player) sender;

						Bukkit.getServer().getOnlinePlayers()
								.stream()
								.filter(player -> player.getWorld().equals(sendPlayer.getWorld()))
								.forEach(player -> giveAchievement(player, achievementNumber));
						sender.sendMessage("【配布完了】No" + args[0] + "の実績をワールド内全員に配布しました。");
					}

					return true;
				case "USER":
					final Player givenPlayer = Bukkit.getPlayer(target);
					if (givenPlayer == null) {
						sender.sendMessage(target + " は現在このサーバーにログインしていません。");
					} else {
						giveAchievement(givenPlayer, achievementNumber);
					}

					return true;
				default:
					sender.sendMessage("全員配布を行いたい場合は、用途に応じてコマンドの最後に以下の記述を追加してください。");
					sender.sendMessage("サーバー全員に配布→「server」、ワールド全員に配布→「world」");
					sender.sendMessage("※もし「ALL」というユーザーが存在し、該当者のみに配布したい場合は最後に「user」と入力してください。");

					return true;
			}
		} else {
			final Player givenPlayer = Bukkit.getPlayer(target);

			if (givenPlayer == null) {
				if (!executedByPlayer) {
					sender.sendMessage(target + "は現在オフラインです。");
					sender.sendMessage("コンソール実行ではオフラインプレイヤーへの予約付与システムは利用できません。");
				} else {
					sender.sendMessage(target + " は現在サーバーにいないため、予約付与システムを利用します。");
					if (sql.writegiveachvNo((Player) sender, target, String.valueOf(achievementNumber))) {
						sender.sendMessage(target + "へ、実績No" + achievementNumber + "の付与の予約が完了しました。");
					}
				}

				return true;
			}

			switch (option.toUpperCase()) {
				case "GIVE":
					if (giveAchievement(givenPlayer, achievementNumber)) {
						sender.sendMessage("【配布完了】No" + args[0] + "の実績を配布しました。");
					} else {
						sender.sendMessage("既に該当実績を獲得しています。");
					}

					return true;
				case "DEPRIVE":
					if (depriveAchievement(givenPlayer, achievementNumber)) {
						sender.sendMessage("【剥奪完了】No" + args[0] + "の実績を剥奪しました。");
					} else {
						sender.sendMessage("該当実績を獲得していません。");
					}

					return true;
				default:
					sender.sendMessage("実績を付与したい場合は「give」を、剥奪したい場合は「deprive」を、");
					sender.sendMessage("それぞれコマンドの最後に入力してください。");

					return true;
			}
		}
	}

	private boolean giveAchievement(Player player, int achievementNumber) {
		final PlayerData playerData = playerDataMap.get(player.getUniqueId());
		if (playerData.TitleFlags.get(achievementNumber)) return false;

		playerData.TitleFlags.set(achievementNumber);
		player.sendMessage("運営チームよりNo" + achievementNumber + "の実績が配布されました。");

		return true;
	}

	private boolean depriveAchievement(Player player, int achievementNumber) {
		final PlayerData playerData = playerDataMap.get(player.getUniqueId());
		if (!playerData.TitleFlags.get(achievementNumber)) return false;

		playerData.TitleFlags.set(achievementNumber, false);

		return true;
	}

	private static Optional<Integer> tryToParseInt(String string) {
		try {
			return Optional.of(Integer.parseInt(string));
		} catch (NumberFormatException ex) {
			return Optional.empty();
		}
	}

	private static boolean existsInConfig(int achieveNumber) {
		final String title1 = SeichiAssist.config.getTitle1(achieveNumber);
		final String title2 = SeichiAssist.config.getTitle2(achieveNumber);
		final String title3 = SeichiAssist.config.getTitle3(achieveNumber);

		return Stream.of(title1, title2, title3)
				.anyMatch(title -> title != null && !title.equals(""));
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
		return null; // :(
	}

}
