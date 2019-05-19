package com.github.unchama.seichiassist.commands;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.listener.new_year_event.NewYearBagListener;
import com.github.unchama.seichiassist.listener.new_year_event.NewYearItemListener;
import com.github.unchama.seichiassist.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by karayuu on 2017/12/05
 * Developer of Gigantic☆Seichi Server
 * Support at dev-basic or dev-extreme channel of Discord
 */
public class EventCommand implements TabExecutor {

	SeichiAssist plugin;

	public EventCommand(SeichiAssist plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
		if (!(commandSender instanceof Player)) {
			return false;
		}

		if (args.length == 0) {
			return false;
		} else if (args[0].equalsIgnoreCase("get")) {
			Player player = (Player) commandSender;
			if (Util.isPlayerInventoryFull(player)) {
				Util.dropItem(player, NewYearBagListener.getNewYearBag());
				Util.dropItem(player, NewYearItemListener.getNewYearApple());
			} else {
				Util.addItem(player, NewYearBagListener.getNewYearBag());
				Util.addItem(player, NewYearItemListener.getNewYearApple());
			}
			return true;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
		return null;
	}
}
