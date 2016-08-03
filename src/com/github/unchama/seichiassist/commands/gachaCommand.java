package com.github.unchama.seichiassist.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.util.Util;

public class gachaCommand implements TabExecutor{
	public SeichiAssist plugin;


	public gachaCommand(SeichiAssist plugin){
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

		if (!(sender instanceof Player)) {
			sender.sendMessage("このコマンドはゲーム内から実行してください。");
			return true;
		}



		Player player = (Player) sender;

		if(args.length == 0){
			return false;
		}else if(args[0].equalsIgnoreCase("add")){
			if(args.length != 2){
				sender.sendMessage("/gacha add 0.05  のように、追加したいアイテムの出現確率を入力してください。");
				return true;
			}
			double probability = Util.toDouble(args[1]);
			Gachaadd(player,probability);
			return true;
		}else if(args[0].equalsIgnoreCase("remove")){
			if(args.length != 2){
				sender.sendMessage("/gacha remove 2 のように、削除したいリスト番号を入力してください");
				return true;
			}
			int num = Util.toInt(args[1]);
			Gacharemove(player,num);
			return true;
		}else if(args[0].equalsIgnoreCase("list")){
			if(args.length != 1){
				sender.sendMessage("/gacha list で現在登録されているガチャアイテムを全て表示します。");
			}
			if(SeichiAssist.gachadatalist.isEmpty()){
				sender.sendMessage("ガチャが設定されていません。");
				return true;
			}
			Gachalist(player);
			return true;
		}else if(args[0].equalsIgnoreCase("clear")){
			if(args.length != 1){
				sender.sendMessage("/gacha clear で現在登録されているガチャアイテムを削除します。");
			}
			Gachaclear(player);
			return true;
		}

		return false;
	}




	private void Gachaadd(Player player,double probability) {
		GachaData gachadata = new GachaData();
		PlayerInventory inventory = player.getInventory();
		gachadata.itemstack = inventory.getItemInMainHand();
		gachadata.amount = inventory.getItemInMainHand().getAmount();
		gachadata.probability = probability;

		SeichiAssist.gachadatalist.add(gachadata);
		player.sendMessage(gachadata.itemstack.getType().toString() + gachadata.amount + "個を確率" + gachadata.probability + "としてガチャに追加しました。");
	}
	private void Gachalist(Player player){
		int i = 1;
		player.sendMessage("アイテム番号|アイテム名|アイテム数|出現確率");
		for (GachaData gachadata : SeichiAssist.gachadatalist) {
			player.sendMessage(i + "|" + gachadata.itemstack.getType().toString() + "|" + gachadata.amount + "|" + gachadata.probability);
			i++;
		}
	}
	private void Gacharemove(Player player,int num) {
		if(SeichiAssist.gachadatalist.size() < num){
			player.sendMessage("listの数以下を指定してください");
		}
		GachaData gachadata = SeichiAssist.gachadatalist.get(num-1);
		player.sendMessage(num + "|" + gachadata.itemstack.getType().toString() + "|" + gachadata.probability + "を削除しました。");
		SeichiAssist.gachadatalist.remove(num-1);
	}
	private void Gachaclear(Player player) {
		SeichiAssist.gachadatalist.clear();
		player.sendMessage("すべて削除しました。");
	}

}
