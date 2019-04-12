package com.github.unchama.seichiassist.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.util.Util;

public class stickCommand implements TabExecutor {
	SeichiAssist plugin;

	public stickCommand(SeichiAssist _plugin){
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
		//プレイヤーを取得
		Player player = (Player)sender;
		//プレイヤーネーム
		//String name = Util.getName(player);
		//UUIDを取得
		//UUID uuid = player.getUniqueId();
		//playerdataを取得
		//PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//プレイヤーからの送信でない時処理終了
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GREEN + "このコマンドはゲーム内から実行してください。");
			return true;
		}else if(args.length == 0){
			//コマンド長が０の時の処理
			ItemStack itemstack = new ItemStack(Material.STICK,1);
			itemstack.setAmount(1); //念のため追加
			if(!Util.isPlayerInventryFill(player)){
				Util.addItem(player,itemstack);
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, (float)0.1, (float)1);
			}else{
				Util.dropItem(player,itemstack);
			}
			return true;
		}
		return false;
	}
}
