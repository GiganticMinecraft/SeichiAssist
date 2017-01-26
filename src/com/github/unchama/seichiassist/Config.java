package com.github.unchama.seichiassist;

import org.bukkit.configuration.file.FileConfiguration;

import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.util.Util;

public class Config{
	private static FileConfiguration config;
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

	//コンフィグのセーブ
	public void saveConfig(){
		plugin.saveConfig();
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
		int i;
		for (i=1; i <= num; i++ ) {
			GachaData gachadata = new GachaData();
			gachadata.itemstack = config.getItemStack("item" + i);
			gachadata.amount = config.getInt("amount" + i);
			gachadata.probability = config.getDouble("probability" + i);
			SeichiAssist.gachadatalist.add(gachadata);
			//plugin.getLogger().info(i + "番目のガチャデータロード完了");
		}
		plugin.getLogger().info("合計" + (i-1) + "個のガチャデータのLoadを完了しました");
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
	public int getDualBreaklevel(){
		return Util.toInt(config.getString("dualbreaklevel"));
	}
	public int getTrialBreaklevel(){
		return Util.toInt(config.getString("trialbreaklevel"));
	}
	public int getExplosionlevel(){
		return Util.toInt(config.getString("explosionlevel"));
	}
	public int getThunderStormlevel() {
		return Util.toInt(config.getString("thunderstormlevel"));
	}
	public int getBlizzardlevel() {
		return Util.toInt(config.getString("blizzardlevel"));
	}
	public int getMeteolevel() {
		return Util.toInt(config.getString("meteolevel"));
	}
	public int getGravitylevel() {
		return Util.toInt(config.getString("gravitylevel"));
	}

	public int getMultipleIDBlockBreaklevel(){
		return Util.toInt(config.getString("multipleidblockbreaklevel"));
	}

	public double getDropExplevel(int i){
		return Util.toDouble(config.getString("dropexplevel" + i,""));
	}

	public int getPassivePortalInventorylevel() {
		return Util.toInt(config.getString("passiveportalinventorylevel"));
	}
	public int getDokodemoEnderlevel() {
		return Util.toInt(config.getString("dokodemoenderlevel"));
	}
	public int getMineStacklevel(int i) {
		return Util.toInt(config.getString("minestacklevel" + i,""));
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

	public String getLvMessage(int i) {
		return config.getString("lv" + i + "message","");
	}

	public String getTitle(int i){
		return config.getString("Achv" + i,"");
	}

	//サーバー番号取得
	public int getServerNum() {
		return Util.toInt(config.getString("servernum"));
	}

	//サブホーム最大数取得
	public int getSubHomeMax() {
		return Util.toInt(config.getString("subhomemax"));
	}

	public int getDebugMode() {
		return Util.toInt(config.getString("debugmode"));
	}

	public int getMebiusDebug() {
		return Util.toInt(config.getString("mebiusdebug"));
	}

	public int rateGiganticToRingo() {
		return Util.toInt(config.getString("rategigantictoringo"));
	}

}