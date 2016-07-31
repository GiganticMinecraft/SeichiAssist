package com.github.unchama.seichiassist;

import static com.github.unchama.seichiassist.Util.*;

import org.bukkit.configuration.file.FileConfiguration;

import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.PlayerData;

public class Config{
	private FileConfiguration config;
	private SeichiAssist plugin;

	//コンストラクタ
	Config(SeichiAssist _plugin){
		plugin = _plugin;
		saveDefaultConfig();

	}
	//コンフィグのロード
	public void loadConfig(){
		config = getConfig();

	}
	public void reloadConfig(){
		plugin.reloadConfig();
		config = getConfig();
	}



	//plugin.ymlがない時にDefaultのファイルを生成
	public void saveDefaultConfig(){
		plugin.saveDefaultConfig();
	}
	//plugin.ymlファイルからの読み込み
	public FileConfiguration getConfig(){
		return plugin.getConfig();
	}

	public void loadPlayerData() {
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
	public void loadGachaData(){
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

	public double getMinuteMineSpeed(){
		return toDouble(config.getString("minutespeedamount"));
	}
	public double getLoginPlayerMineSpeed(){
		return toDouble(config.getString("onlineplayersamount"));
	}
	public int getGachaPresentInterval(){
		return toInt(config.getString("presentinterval"));
	}
	public int getDefaultMineAmount(){
		return toInt(config.getString("defaultmineamount"));
	}
	public int getActiveMinelevel(){
		return toInt(config.getString("activeminelevel"));
	}
	public int getDropExplevel(){
		return toInt(config.getString("dropexplevel"));
	}
	public String getDB(){
		return config.getString("db");
	}
	public String getTable() {
		return config.getString("table");
	}
	public String getID(){
		return config.getString("id");
	}
	public String getPW(){
		return config.getString("pw");
	}
	public String getURL(){
		String url = "jdbc:mysql://";
		url += config.getString("host");
		if(!config.getString("port").isEmpty()){
			url += ":" + config.getString("port");
		}
		return url;
	}


	public void savePlayerData() {
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

	public void saveGachaData() {
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

	public String getLvMessage(int i) {
		return config.getString("lv" + i + "message");
	}


}