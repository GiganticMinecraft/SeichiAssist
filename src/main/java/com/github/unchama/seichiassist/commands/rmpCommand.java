package com.github.unchama.seichiassist.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.seichiassist.util.ExternalPlugins;
import com.sk89q.worldguard.bukkit.RegionContainer;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class rmpCommand implements TabExecutor {
	static DatabaseGateway databaseGateway = SeichiAssist.databaseGateway;
	private Map<UUID, String> leavers;
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
	String label, String[] args) {
		if (args.length < 3) {
			sender.sendMessage(ChatColor.RED + "/rmp <日数> <削除フラグ: true/false> <world名>");
			sender.sendMessage("全Ownerが<日数>間ログインしていないRegionを表示します");
			sender.sendMessage("削除フラグがtrueの場合、該当Regionを削除します(整地ワールドのみ)");
			return true;
		} else if ((sender instanceof Player)) {
			//コンソールからの送信でない時処理終了
			sender.sendMessage(ChatColor.GREEN + "このコマンドはコンソールから実行してください");
			return true;
		} else {
			int days = 7;
			boolean removeFlg = false;
			String worldName = args[2];

			//<日数>を数値変換
			try {
				if (args.length > 0) days = Integer.parseInt(args[0]);
				//<削除フラグ>を判定(保護を掛けて整地する整地ワールドに限る)
				if ((args.length > 1) && (args[1].equals("true"))) {
					if(SeichiAssist.rgSeichiWorldlist.contains(worldName)) {
						removeFlg = true;
					} else {
						sender.sendMessage(ChatColor.RED + "削除フラグは保護をかけて整地する整地ワールドでのみ使用出来ます");
					}
				}
				//mysqlからログインしていないプレイヤーリストを取得
				leavers = databaseGateway.playerDataManipulator.selectLeavers(days);
				if (leavers == null) {
					//DBエラー
					sender.sendMessage(ChatColor.RED + "失敗");
					return true;
				}
				//コマンドで指定されたワールドの全Regionを取得する
				final RegionContainer regionContainer = ExternalPlugins.getWorldGuard().getRegionContainer();
				// (イミュータブル)
				final Map<String, ProtectedRegion> regions = regionContainer.get(Bukkit.getWorld(worldName)).getRegions();

				//__global__ は除外
				//spawn も除外
				//結果格納用List
				final List<String> removalTargets = regions.entrySet().parallelStream()
						.filter(entry -> {
							final String regionName = entry.getKey();
							final ProtectedRegion region = entry.getValue();
							return !regionName.equals("__global__") && !regionName.equals("spawn") && isAllLeave(region.getOwners());
						})
						.map(Map.Entry::getKey)
						.collect(Collectors.toList());
				//結果処理
				if (removalTargets.size() == 0) {
					sender.sendMessage(ChatColor.GREEN + "該当Regionは存在しません");
				} else if (removeFlg) {
					//該当領域削除
					removalTargets.forEach(target -> {
						regionContainer.get(Bukkit.getWorld(worldName)).removeRegion(target);
						sender.sendMessage(ChatColor.YELLOW.toString() + "[rmp] Deleted Region -> " + worldName + "." + target);
					});
				
				} else {
					//一覧表示
					removalTargets.forEach(target -> sender.sendMessage(ChatColor.GREEN.toString() + "[rmp] List Region -> " + worldName + "." + target));
				}
			} catch (NumberFormatException e) {
				//parseIntエラー
				sender.sendMessage(ChatColor.RED + "<日数>には整数を入力してください");
				return true;
			}
			return true;
		}
	}

	//Region内の全OwnerがLeaverかを判定する
	private boolean isAllLeave(DefaultDomain domain) {
		//Owner一覧からIDリストを取得
		Set<UUID> owners = domain.getUniqueIds();
		//各Ownerに対してチェック
		for(UUID owner : owners) {
			//OwnerがLeaverに含まれているか
			if (!leavers.containsKey(owner)) return false;
		}
		return true;
	}
}
