package com.github.unchama.seichiassist;

import static com.github.unchama.seichiassist.Util.*;

import org.bukkit.configuration.file.FileConfiguration;

public class Config{
	public static FileConfiguration config;
	public static SeichiAssist plugin;
	public Config(SeichiAssist _plugin){
		plugin = _plugin;
		saveDefaultConfig();
		config = getConfig();
		loadGachaData();
	}

	public static void reloadConfig(){
		plugin.reloadConfig();
		config = getConfig();
		loadGachaData();
	}
	//plugin.ymlがない時にDefaultのファイルを生成
	public void saveDefaultConfig(){
		plugin.saveDefaultConfig();
	}
	//plugin.ymlファイルからの読み込み
	public static FileConfiguration getConfig(){
		return plugin.getConfig();
	}
	//plugin.ymlファイルからガチャデータの読み込み
	public static void loadGachaData(){
		int num = config.getInt("num");
		for (int i=1; i < num; i++ ) {
			GachaData gachadata = new GachaData();
			gachadata.itemstack = config.getItemStack("item" + i);
			gachadata.amount = config.getInt("amount" + i);
			gachadata.itemstack = config.getItemStack("probability" + i);
			SeichiAssist.gachadatalist.add(gachadata);
		}
		plugin.getLogger().info("ガチャデータのLoadを完了しました。");
	}

	public static double getMinuteMineSpeed(){
		return toDouble(config.getString("minutespeedamount"));
	}
	public static double getLoginPlayerMineSpeed(){
		return toDouble(config.getString("onlineplayersamount"));
	}
	public static int getGachaPresentInterval(){
		return toInt(config.getString("presentinterval"));
	}
	public static int getDefaultMineAmount(){
		return toInt(config.getString("defaultmineamount"));
	}
}