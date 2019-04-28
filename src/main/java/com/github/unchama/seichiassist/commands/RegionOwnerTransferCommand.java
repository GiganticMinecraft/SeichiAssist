package com.github.unchama.seichiassist.commands;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * 保護のオーナー権限を引き渡すコマンド。
 */
public class RegionOwnerTransferCommand implements CommandExecutor, TabExecutor {
	@Override
	// /x-transfer [name] [to Player]
	public boolean onCommand(CommandSender sender, Command command, String actualCommand, String[] args) {
		if (args.length != 2) {
			return false;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage("このコマンドはプレイヤーから実行してください。");
			return true;
		}

		final Player player = (Player) sender;
		final String regionName = args[0];
		final RegionManager rm = WorldGuardPlugin.inst().getRegionManager(player.getWorld());
		if (!rm.hasRegion(regionName)) {
			player.sendMessage(regionName + "という名前の保護は存在しません。");
			return true;
		}

		final ProtectedRegion c = rm.getRegion(regionName);
		if (c == null) {
			player.sendMessage("エラーが発生しました。管理者に報告してください。");
			return true;
		}

		if (!c.getOwners().contains(player.getUniqueId())) {
			// permission denied
			player.sendMessage("オーナーではないため権限を譲渡できません。");
			return true;
		}

		if (c.getOwners().size() != 1) {
			// unsupported
			player.sendMessage("オーナーが複数人いるため権限を譲渡できません。");
			return true;
		}

		final String newOwner = args[1];
		final Player maybeTarget = Bukkit.getPlayer(newOwner);
		if (maybeTarget == null) {
			player.sendMessage(newOwner + "というプレイヤーはサーバーに参加したことがありません。");
			return true;
		}

		c.getOwners().clear();
		c.getOwners().addPlayer(maybeTarget.getUniqueId());
		player.sendMessage(newOwner + "に" + regionName + "のオーナー権限を譲渡しました。");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] string) {
		return null;
	}
}
