package com.github.unchama.seichiassist.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.github.unchama.seichiassist.Level;
import com.github.unchama.seichiassist.SeichiAssist;

public class levelCommand implements TabExecutor{
	public SeichiAssist plugin;


	public levelCommand(SeichiAssist plugin){
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
		if(args[0].equalsIgnoreCase("reset")){
				if(args.length != 1){
					sender.sendMessage("/level resetで全員のレベル計算をリセットし、レベルアップを再度可能にします");
					return true;
				}
				for(String name :SeichiAssist.playermap.keySet()){
					Level.setLevel(name,1);
					sender.sendMessage(name+"のレベルを" + Level.getLevel(name) + "に設定しました");
					Level.reloadLevel(name);
					return true;
				}
			}
		return false;
	}



}
