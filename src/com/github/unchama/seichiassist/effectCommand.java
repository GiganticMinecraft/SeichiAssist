package com.github.unchama.seichiassist;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class effectCommand implements TabExecutor {
	SeichiAssist plugin;

	public effectCommand(SeichiAssist _plugin){
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

		if (!(sender instanceof Player)) {
			sender.sendMessage("このコマンドはゲーム内から実行してください。");
			return true;
		}else if(args.length == 0){
			Player player = (Player)sender;
			PlayerData playerdata = SeichiAssist.playermap.get(player);
			playerdata.effectflag = !playerdata.effectflag;
			if (playerdata.effectflag){
				sender.sendMessage("採掘速度上昇効果をONにしました。");
			}else{
				sender.sendMessage("採掘速度上昇効果をOFFにしました。ONに戻したい時は再度コマンドを実行します。");
			}
			return true;
		}
		return false;
	}

}
