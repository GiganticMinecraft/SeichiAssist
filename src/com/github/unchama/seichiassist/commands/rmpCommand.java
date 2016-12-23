package com.github.unchama.seichiassist.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.util.Util;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.md_5.bungee.api.ChatColor;

public class rmpCommand implements TabExecutor {
	static Sql sql = SeichiAssist.plugin.sql;
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
		//rmp <Days> 以外の呼び出し
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "/rmp <日数>");
			sender.sendMessage("全Ownerが<日数>間ログインしていないRegionを表示します");
			return true;
		} else {
			try {
				//<日数>を数値変換
				int days = Integer.parseInt(args[0]);
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
					//Region内の全OwnerがLeaverなら該当するRegionのIDを結果Listに格納する
					if (isAllLeave(regions.get(id).getOwners())) targets.add(id);
				}

				//結果表示
				targets.forEach(target -> {
					sender.sendMessage(ChatColor.YELLOW.toString() + target);
					//領域削除機能…要/rg remove権限
//					((Player)sender).chat("/rg remove " + target);
				});
			} catch (NumberFormatException e) {
				//parseIntエラー
				sender.sendMessage("<日数>には整数を入力してください");
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
