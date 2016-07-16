package com.github.unchama.multiseichieffect;

import static com.github.unchama.multiseichieffect.Util.*;

import java.util.HashMap;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class gachaCommand implements TabExecutor{
	public MultiSeichiEffect plugin;
	private HashMap<ItemStack,Double> itemlist;


	public gachaCommand(MultiSeichiEffect plugin){
		this.plugin = plugin;
		itemlist = MultiSeichiEffect.gachaitem;;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String label, String[] args) {
		double probability = 0;

		if(args.length != 2){
			sender.sendMessage("引数は2つにして下さい");
			return true;

		}else if(args.length == 2){
			if(args[0].equalsIgnoreCase("set")){
				probability = toDouble(args[1]);
				if (!(sender instanceof Player)) {
					sender.sendMessage("このコマンドはゲーム内から実行してください。");
				} else {
					Player player = (Player) sender;
					Gachaset(player,toDouble(args[1]));
				}

				return true;
			}
		}

		return false;
	}

	private void Gachaset(Player player,Double probability) {
		ItemStack itemstack;
		itemstack = player.getItemOnCursor();
		itemlist.put(itemstack,probability);
	}

}
