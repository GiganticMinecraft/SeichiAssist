package com.github.unchama.seichiassist.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.util.Util;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class rmpCommand implements TabExecutor {
	static Sql sql = SeichiAssist.sql;
	private Map<UUID, String> leavers;

	public rmpCommand(SeichiAssist plugin){
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
	String label, String[] args) {
		if (args.length > 2) {
			sender.sendMessage(ChatColor.RED + "/rmp <日数> <削除フラグ: true/false>");
			sender.sendMessage("全Ownerが<日数>間ログインしていないRegionを表示します");
			sender.sendMessage("削除フラグがtrueの場合、該当Regionを削除します(整地ワールドのみ)");
			return true;
		} else if (!(sender instanceof Player)) {
			//プレイヤーからの送信でない時処理終了
			sender.sendMessage(ChatColor.GREEN + "このコマンドはゲーム内から実行してください");
			return true;
		} else {
			try {
				int days = 7;
				boolean removeFlg = false;

				//<日数>を数値変換
				if (args.length > 0) days = Integer.parseInt(args[0]);
				//<削除フラグ>を判定(保護を掛けて整地する整地ワールドに限る)
				if ((args.length > 1) && (args[1].equals("true"))) {
					if(SeichiAssist.rgSeichiWorldlist.contains(((Player)sender).getWorld().getName())) {
						removeFlg = true;
					} else {
						sender.sendMessage(ChatColor.RED + "削除フラグは保護をかけて整地する整地ワールドでのみ使用出来ます");
					}
				}
				//mysqlからログインしていないプレイヤーリストを取得
				leavers = sql.selectLeavers(days);
				if (leavers == null) {
					//DBエラー
					sender.sendMessage(ChatColor.RED + "失敗");
					return true;
				}

				//実行者のいるワールドに存在する全Regionを取得する
				Map<String, ProtectedRegion> regions = Util.getWorldGuard().getRegionContainer().get(((Player)sender).getWorld()).getRegions();
				//結果格納用List
				List<String> targets = new ArrayList<String>();

				//各Regionに対してチェック
				for (String id : regions.keySet()) {
					//__global__Regionは除外
					if (id.equals("__global__")) continue;
					//spawnRegionも除外
					if (id.equals("spawn")) continue;
					//Region内の全OwnerがLeaverなら該当するRegionのIDを結果Listに格納する
					if (isAllLeave(regions.get(id).getOwners())) targets.add(id);
				}

				//結果処理
				if (targets.size() == 0) {
					sender.sendMessage(ChatColor.GREEN + "該当Regionは存在しません");
				} else if (removeFlg) {
					//該当領域削除
					targets.forEach(target -> {
						((Player)sender).chat("/rg remove " + target);
					});
				} else {
					//一覧表示
					targets.forEach(target -> {
						sender.sendMessage(ChatColor.YELLOW.toString() + target);
					});
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
