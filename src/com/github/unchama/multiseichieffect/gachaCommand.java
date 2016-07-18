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


	public gachaCommand(MultiSeichiEffect plugin,HashMap<ItemStack,Double> gachaitem){
		this.plugin = plugin;
		itemlist = gachaitem;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String label, String[] args) {
		return null;
	}
	// /gacha set 0.01 (現在手にもってるアイテムが確率0.01でガチャに出現するように設定）
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
					Gachaset(player,probability);
					sender.sendMessage(player.getInventory().getItemInMainHand().getType().toString() + player.getInventory().getItemInMainHand().getAmount() + "個を確率" + Decimal(probability) + "としてガチャに追加しました。");
				}

				return true;
			}
		}

		return false;
	}


	private void Gachaset(Player player,Double probability) {
		ItemStack itemstack;
		itemstack = player.getInventory().getItemInMainHand();
		itemlist.put(itemstack,probability);
	}

}
