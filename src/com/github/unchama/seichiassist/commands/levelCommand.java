package com.github.unchama.seichiassist.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.github.unchama.seichiassist.Level;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;

public class levelCommand implements TabExecutor{
	public SeichiAssist plugin;
	Sql sql = SeichiAssist.plugin.sql;


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
			List<String> namelist = sql.getNameList(SeichiAssist.PLAYERDATA_TABLENAME);

			for(String name : namelist){
				name = sql.selectstring(SeichiAssist.PLAYERDATA_TABLENAME,name, "name");
				Level.setLevel(name,1);
				sender.sendMessage(name+"のレベルを" + Level.getLevel(name) + "に設定しました");
				Level.reloadLevel(name);
			}

		}
		return false;
	}



}
