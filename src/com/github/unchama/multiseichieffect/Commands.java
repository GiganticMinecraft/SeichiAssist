package com.github.unchama.multiseichieffect;

import java.util.HashMap;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class Commands extends JavaPlugin{
	MultiSeichiEffect plugin;
	private HashMap<String, TabExecutor> commandlist;
	
	//コンストラクタ
	Commands(MultiSeichiEffect _plugin){
		plugin = _plugin;
		commandlist = new HashMap<String, TabExecutor>();
		addCommand("gacha",new gachaCommand(plugin));
		addCommand("seichi",new seichiCommand(plugin));
		addCommand("ef",new effectCommand(plugin));
		//(/unchama ~)のようなコマンドを追加したい場合はこれ以降に追加
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		return commandlist.get(cmd.getName()).onCommand(sender, cmd, label, args);
	}
	
	
	private void addCommand(String command,TabExecutor classname) {
		commandlist.put(command,classname);
	}
}
