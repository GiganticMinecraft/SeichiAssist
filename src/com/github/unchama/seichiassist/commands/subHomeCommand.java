package com.github.unchama.seichiassist.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class subHomeCommand implements TabExecutor {
	SeichiAssist plugin;

	public subHomeCommand(SeichiAssist _plugin){
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
		Player player = (Player)sender;
		String name = Util.getName(player);
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		int maxsubhome = SeichiAssist.config.getSubHomeMax();
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GREEN + "このコマンドはゲーム内から実行してください。");
			return true;
		}
		else if(args.length == 0 || args[0].equalsIgnoreCase("help")){
			sender.sendMessage(ChatColor.GREEN + "/subhome コマンドの使い方");
			sender.sendMessage(ChatColor.GREEN + "移動する場合");
			sender.sendMessage(ChatColor.GREEN + "/subhome [移動したいサブホームの番号]");
			sender.sendMessage(ChatColor.GREEN + "セットする場合");
			sender.sendMessage(ChatColor.GREEN + "/subHome set [移動したいサブホームの番号]");
			sender.sendMessage(ChatColor.GREEN + "名前変更する場合");
			sender.sendMessage(ChatColor.GREEN + "/subHome name [移動したいサブホームの番号]");
		}
		else if(args.length == 1){
			try{
				int num = Util.toInt(args[0]);
				if(num >= 1 && maxsubhome >= num){
					Location l = playerdata.GetSubHome(num-1);
					if(l != null){
						World world = Bukkit.getWorld(l.getWorld().getName());
						if(world != null){
							player.teleport(l);
							player.sendMessage("サブホームポイント"+ (num) +"にワープしました");
						}
						else{
							player.sendMessage("サブホームポイント"+ (num) +"が設定されてません");
						}
					}
					else{
						player.sendMessage("サブホームポイント"+ (num) +"が設定されてません");
					}
					return true;
				}
				else {
					player.sendMessage("サブホームの番号を1～" + maxsubhome + "の間で入力してください");
					return true;
				}
			}catch(NumberFormatException e){
				player.sendMessage("サブホームの番号を1～" + maxsubhome + "の間で入力してください");
				return true;
			}
		}
		else if(args.length == 2){
			try{
				int num = Util.toInt(args[1]);
				if(num >= 1 && maxsubhome >= num){
					if(args[0].equalsIgnoreCase("set")){
						playerdata.SetSubHome(player.getLocation(), num-1);
						player.sendMessage("現在位置をサブホームポイント"+(num)+"に設定しました");
						return true;
					}
					else if(args[0].equalsIgnoreCase("name")){
						player.sendMessage("サブホームポイント" + num + "に設定する名前をチャットで入力してください");
						player.sendMessage(ChatColor.YELLOW + "※入力されたチャット内容は他のプレイヤーには見えません");
						playerdata.setHomeNameNum = num-1;
						playerdata.isSubHomeNameChange = true;
						return true;
					}
				}
				else {
					player.sendMessage("サブホームの番号を1～" + maxsubhome + "の間で入力してください");
					return true;
				}
			}catch(NumberFormatException e){
				player.sendMessage("サブホームの番号を1～" + maxsubhome + "の間で入力してください");
				return true;
			}
		}

		return false;
	}
}