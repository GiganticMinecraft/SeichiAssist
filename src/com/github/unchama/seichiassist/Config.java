package com.github.unchama.seichiassist;

import static com.github.unchama.seichiassist.Util.*;

import org.bukkit.configuration.file.FileConfiguration;

import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.PlayerData;

public class Config{
	public static FileConfiguration config;
	public static SeichiAssist plugin;
	public Config(SeichiAssist _plugin){
		plugin = _plugin;
		saveDefaultConfig();
		config = getConfig();
		loadGachaData();
		loadPlayerData();
	}

	public static void reloadConfig(){
		plugin.reloadConfig();
		config = getConfig();
		loadGachaData();
		loadPlayerData();
	}



	//plugin.ymlがない時にDefaultのファイルを生成
	public void saveDefaultConfig(){
		plugin.saveDefaultConfig();
	}
	//plugin.ymlファイルからの読み込み
	public static FileConfiguration getConfig(){
		return plugin.getConfig();
	}

	private static void loadPlayerData() {
		int num = config.getInt("playernum");
		for (int i=1; i < num; i++ ) {
			String name = config.getString("player"+i);
			if(SeichiAssist.playermap.containsKey(name)){
				continue;
			}
			PlayerData d = new PlayerData();
			d.effectflag = config.getBoolean(name + "effectflag");
			d.messageflag = config.getBoolean(name + "messageflag");
			d.gachapoint = config.getInt(name + "gachapoint");
			d.level = config.getInt(name + "level");
			d.numofsorryforbug = config.getInt(name + "numofsorryforbug");
			SeichiAssist.playermap.put(name,d);
		}
		plugin.getLogger().info("プレイヤーデータのLoadを完了しました。");
	}
	//plugin.ymlファイルからガチャデータの読み込み
	public static void loadGachaData(){
		int num = config.getInt("gachanum");
		for (int i=1; i < num; i++ ) {
			GachaData gachadata = new GachaData();
			gachadata.itemstack = config.getItemStack("item" + i);
			gachadata.amount = config.getInt("amount" + i);
			gachadata.probability = config.getDouble("probability" + i);
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
	public static int getActiveMinelevel(){
		return toInt(config.getString("activeminelevel"));
	}
	public static int getDropExplevel(){
		return toInt(config.getString("dropexplevel"));
	}


	public static void savePlayerData() {
		//プレイヤーのデータをセーブ
		int i = 1;
		for(String name : SeichiAssist.playermap.keySet()){
			PlayerData d = SeichiAssist.playermap.get(name);
			config.set("player" + i,name);
			config.set(name + "effectflag",d.effectflag);
			config.set(name + "messageflag",d.messageflag);
			config.set(name + "gachapoint",d.gachapoint);
			config.set(name + "level",d.level);
			config.set(name + "numofsorryforbug", d.numofsorryforbug);
			i++;
		}
		config.set("playernum",i);
	}

	public static void saveGachaData() {
		//ガチャのデータを保存
		int i = 1;
		for(GachaData gachadata : SeichiAssist.gachadatalist){
			config.set("item"+ i,gachadata.itemstack);
			config.set("amount"+ i,gachadata.amount);
			config.set("probability"+ i,gachadata.probability);
			i++;
		}
		config.set("gachanum",i);
	}

	public static String getLvMessage(int i) {
		return config.getString("lv" + i + "message");
	}
}