package com.github.unchama.seichiassist;

import org.bukkit.configuration.file.FileConfiguration;

import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.util.Util;

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
	//コンフィグのリロード
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
		return Util.toDouble(config.getString("minutespeedamount"));
	}
	public double getLoginPlayerMineSpeed(){
		return Util.toDouble(config.getString("onlineplayersamount"));
	}
	public int getGachaPresentInterval(){
		return Util.toInt(config.getString("presentinterval"));
	}
	public int getDefaultMineAmount(){
		return Util.toInt(config.getString("defaultmineamount"));
	}
	public int getActiveMinelevel(){
		return Util.toInt(config.getString("activeminelevel"));
	}
	public int getDropExplevel(){
		return Util.toInt(config.getString("dropexplevel"));
	}
	public int getPassivePortalInventorylevel() {
		return Util.toInt(config.getString("passiveportalinventorylevel"));
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