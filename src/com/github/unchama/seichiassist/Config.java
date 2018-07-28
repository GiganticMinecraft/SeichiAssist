package com.github.unchama.seichiassist;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

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

	public String getTitle1(int i){
		return config.getString("AchvA" + i,"");
	}

	public String getTitle2(int i){
		return config.getString("AchvB" + i,"");
	}

	public String getTitle3(int i){
		return config.getString("AchvC" + i,"");
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

	public boolean isGridProtectForce(Player player) {
		List<String> worldlist = config.getStringList("GridProtectForceWorld");

		for (String name : worldlist) {
			if (player.getWorld().getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	public int getGridLimit() {
		return Util.toInt(config.getString("GridLimit"));
	}

	public int getTemplateKeepAmount() {
		return Util.toInt(config.getString("GridTemplateKeepAmount"));
	}

	public int getRoadY() {
		return config.getInt("road_Y");
	}

	public int getRoadLength() {
		return config.getInt("road_length");
	}

	public int getSpaceHeight() {
		return config.getInt("space_height");
	}

	public int getRoadBlockID() {
		return config.getInt("road_blockid");
	}

	public int getRoadBlockDamage() {
		return config.getInt("road_blockdamage");
	}

	public int getContributeAddedMana() {
		return config.getInt("contribute_added_mana");
	}

	public String getGivingNewYearSobaDay() {
		return config.getString("NewYearEvent.GivingNewYearSobaDay");
	}

	public String getNewYearSobaYear() {
		return config.getString("NewYearEvent.NewYearSobaYear");
	}

	public String getDropNewYearBagStartDay() {
		return config.getString("NewYearEvent.DropNewYearBagStartDay");
	}

	public String getDropNewYearBagEndDay() {
		return config.getString("NewYearEvent.DropNewYearBagEndDay");
	}

	public int getNewYearDropProbability() {
		return config.getInt("NewYearEvent.NewYearBagDropProbability");
	}

	public String getNewYear() {
		return config.getString("NewYearEvent.NewYear");
	}

	public String getNewYearAppleStartDay() {
		return config.getString("NewYearEvent.NewYearAppleStartDay");
	}

	public String getNewYearAppleEndDay() {
		return config.getString("NewYearEvent.NewYearAppleEndDay");
	}

	public int getWorldSize() {
		return config.getInt("world_size");
	}

	public int getGiganticFeverMinutes() {
	    return config.getInt("gigantic_fever_minutes");
    }

    public String getGiganticFeverDisplayTime() {
	    int minute = getGiganticFeverMinutes();

	    int hours = minute / 60;
	    int minutes = minute - 60 * hours;

	    return hours + "時間" + minutes + "分";
    }
}
