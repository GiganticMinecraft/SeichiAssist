package com.github.unchama.multiseichieffect;

import static com.github.unchama.multiseichieffect.Util.*;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class gachaCommand implements TabExecutor{
	public MultiSeichiEffect plugin;


	public gachaCommand(MultiSeichiEffect plugin){
		this.plugin = plugin;
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
		Player player = (Player) sender;



		if (!(sender instanceof Player)) {
			sender.sendMessage("このコマンドはゲーム内から実行してください。");
		}else if(args.length == 0){
			return false;
		}else if(args[0].equalsIgnoreCase("add")){
			if(args.length == 1){
				sender.sendMessage("/gacha add 0.05  のように、追加したいアイテムの出現確率を入力してください。");
				return true;
			}
			double probability = toDouble(args[1]);
			Gachaadd(player,probability);
			return true;
		}else if(args[0].equalsIgnoreCase("remove")){
			if(args.length == 1){
				sender.sendMessage("/gacha remove 2 のように、削除したいリスト番号を入力してください");
				return true;
			}
			int num = toInt(args[1]);
			Gacharemove(player,num);
			return true;
		}else if(args[0].equalsIgnoreCase("list")){
			if(MultiSeichiEffect.gachaitem.isEmpty()){
				sender.sendMessage("ガチャが設定されていません");
				return true;
			}
			Gachalist(player);
			return true;
		}else if(args[0].equalsIgnoreCase("clear")){
			Gachaclear(player);
			return true;
		}

		return false;
	}



	private void Gachaadd(Player player,Double probability) {
		ItemStack itemstack;
		itemstack = player.getInventory().getItemInMainHand();
		MultiSeichiEffect.gachaitem.put(itemstack,probability);
		player.sendMessage(player.getInventory().getItemInMainHand().getType().toString() + player.getInventory().getItemInMainHand().getAmount() + "個を確率" + Decimal(probability) + "としてガチャに追加しました。");
	}
	private void Gachalist(Player player){
		int i = 1;
		player.sendMessage("アイテム番号|アイテム名|アイテム数|出現確率");
		for (Entry<ItemStack, Double> item : MultiSeichiEffect.gachaitem.entrySet()) {
			player.sendMessage(i + "|" + item.getKey().getType().toString() + "|" + item.getKey().getAmount() + "|" +item.getValue());
			i++;
		}
	}
	private void Gacharemove(Player player,int num) {
		int i = 1;
		for (Entry<ItemStack, Double> item : MultiSeichiEffect.gachaitem.entrySet()) {
			if(num == i){
				MultiSeichiEffect.gachaitem.remove(item.getKey());
				player.sendMessage(i + "|" + item.getKey().getType().toString() + "|" + item.getValue());
				player.sendMessage("を削除しました.");
				break;
			}
			i++;
		}
	}
	private void Gachaclear(Player player) {
		MultiSeichiEffect.gachaitem.clear();
		player.sendMessage("すべて削除しました。");
	}

}
