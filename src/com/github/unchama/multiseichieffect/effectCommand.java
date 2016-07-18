package com.github.unchama.multiseichieffect;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class effectCommand implements TabExecutor {
	MultiSeichiEffect plugin;

	public effectCommand(MultiSeichiEffect _plugin){
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

		if(args.length == 0){
			if (!(sender instanceof Player)) {
				sender.sendMessage("このコマンドはゲーム内から実行してください。");
			} else {
				Player player = (Player)sender;
				if(MultiSeichiEffect.playerflag.containsKey(player)){
				MultiSeichiEffect.playerflag.put(player,!MultiSeichiEffect.playerflag.get(player));
				}else{
					sender.sendMessage("ログインし直してください。");
					return true;
				}
				if (MultiSeichiEffect.playerflag.get(player)){
					sender.sendMessage("採掘速度上昇効果をONにしました。");
				}else{
					sender.sendMessage("採掘速度上昇効果をOFFにしました。");
				}

				return true;
			}
		}
		return false;
	}

}
