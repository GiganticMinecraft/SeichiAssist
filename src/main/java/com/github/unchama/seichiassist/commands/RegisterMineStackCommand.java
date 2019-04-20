package com.github.unchama.seichiassist.commands;

import com.github.unchama.seichiassist.minestack.MineStackRegistry;
import com.github.unchama.seichiassist.minestack.objects.MineStackBuildObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackDropObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackFarmObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackGachaObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackMineObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackRsObj;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RegisterMineStackCommand implements TabCompleter, CommandExecutor, TabExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("このコマンドはプレイヤーから実行してください。");
			return true;
		}
		final Player player = (Player) sender;
		final ItemStack item = player.getInventory().getItemInMainHand();
		if (item == null) {
			sender.sendMessage("手にアイテムを持ってください。");
			return true;
		}

		// [type] [objname] [level]
		if (args.length != 3) {
			sender.sendMessage("1番目にタイプ、2番目に内部ID、3番目にレベルを入力してください。");
		}

		final String internalId = args[1];
		/* TODO: レベルとは??? */
		final int theLevel = Integer.parseInt(args[2]);
		switch (args[0].toLowerCase()) {
			case "build":
				MineStackRegistry.addBuildingMaterial(new MineStackBuildObj(internalId, null, theLevel, item.getType(), item.getDurability()));
				break;
			case "drop":
				MineStackRegistry.addDropMaterial(new MineStackDropObj(internalId, null, theLevel, item.getType(), item.getDurability()));
				break;
			case "farm":
				MineStackRegistry.addFarmMaterial(new MineStackFarmObj(internalId, null, theLevel, item.getType(), item.getDurability()));
				break;
			case "gacha":
				MineStackRegistry.addGachaMaterial(new MineStackGachaObj(internalId, null, theLevel, item.getType(), item.getDurability()));
				break;
			case "mine":
				MineStackRegistry.addMiningMaterial(new MineStackMineObj(internalId, null, theLevel, item.getType(), item.getDurability()));
				break;
			case "redstone":
				MineStackRegistry.addRedstoneMaterial(new MineStackRsObj(internalId, null, theLevel, item.getType(), item.getDurability()));
				break;
			default:
				sender.sendMessage(" 以下の引数が有効です:\n" +
						"build -> 建材\n" +
						"drop -> ドロップ\n" +
						"farm -> 農業\n" +
						"gacha -> ガチャ\n" +
						"mine -> 採掘\n" +
						"redstone -> レッドストーン\n" +
						""
				);
				break;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}
}
