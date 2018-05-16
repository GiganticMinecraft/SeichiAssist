package com.github.unchama.seichiassist.commands;

import java.util.List;

import com.github.unchama.seichiassist.data.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.listener.MebiusListener;
import sun.management.*;

public class mebiusCommand implements TabExecutor {

	public mebiusCommand(SeichiAssist plugin) {
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			showDescription(sender);
			sender.sendMessage(ChatColor.RED + "コマンドはプレイヤーから実行してください.");
			return true;
		}

		switch (args.length) {
			case 1: {
				switch (args[0]) {
					case "get":
						if (!sender.isOp()) {
							sendPermissionWarning(sender);
							break;
						}
						MebiusListener.debugGive((Player) sender);
						break;
					case "reload":
						if (!sender.isOp()) {
							sendPermissionWarning(sender);
							break;
						}
						MebiusListener.reload();
						break;
					case "debug":
						if (!sender.isOp()) {
							sendPermissionWarning(sender);
							break;
						}
						MebiusListener.debug((Player) sender);
						break;
					case "nickname":
						String name = MebiusListener.getNickname((Player) sender);
						if (name == null) {
							sender.sendMessage(ChatColor.RED + "呼び名の確認はMEBIUSを装着して行ってください.");
						} else {
							sender.sendMessage(ChatColor.GREEN + "現在のメビウスからの呼び名 : " + name);
						}
						break;

					default:
						showDescription(sender);
						break;
				}
				return true;
			}

			case 2: {
				switch (args[0]) {
					case "naming": {
						if (!MebiusListener.setName((Player) sender, args[1])) {
							sender.sendMessage(ChatColor.RED + "命名はMEBIUSを装着して行ってください.");
						}
						return true;
					}

					case "nickname": {
						if (args[1].equals("reset")) {
							if (!MebiusListener.setNickname((Player) sender, sender.getName())) {
								sender.sendMessage(ChatColor.RED + "呼び名のリセットはMEBIUSを装着して行ってください.");
								return true;
							} else {
								sender.sendMessage(ChatColor.GREEN + "メビウスからの呼び名を" + sender.getName() + "にリセットしました.");
								return true;
							}
						}

						if (!MebiusListener.setNickname((Player) sender, args[1])) {
							sender.sendMessage(ChatColor.RED + "呼び名の設定はMEBIUSを装着して行ってください.");
							return true;
						} else {
							sender.sendMessage(ChatColor.GREEN + "メビウスからの呼び名を" + args[1] + "にセットしました.");
							return true;
						}
					}

					default:
						showDescription(sender);
						return true;
				}
			}

			default:
				showDescription(sender);
				break;
		}
		return true;
	}

	private void showDescription(CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "[Usage]");
		sender.sendMessage(ChatColor.RED + "/mebius naming <name>");
		sender.sendMessage(ChatColor.RED + "  現在頭に装着中のMEBIUSに<name>を命名します。");
		sender.sendMessage("");
		sender.sendMessage(ChatColor.RED + "/mebius nickname");
		sender.sendMessage(ChatColor.RED + "  MEBIUSから呼ばれる名前を表示します");
		sender.sendMessage("");
		sender.sendMessage(ChatColor.RED + "/mebius nickname <name>");
		sender.sendMessage(ChatColor.RED + "  MEBIUSから呼ばれる名前を<name>に変更します");
		sender.sendMessage("");
		sender.sendMessage(ChatColor.RED + "/mebius nickname reset");
		sender.sendMessage(ChatColor.RED + "  MEBIUS空の呼び名をプレイヤー名(初期設定)に戻します");
		sender.sendMessage("");
	}

	private void sendPermissionWarning(CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "このコマンドは権限者のみが実行可能です.");
	}
}
