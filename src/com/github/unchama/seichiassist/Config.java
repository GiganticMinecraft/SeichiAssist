package com.github.unchama.seichiassist;

import static com.github.unchama.seichiassist.Util.*;

import org.bukkit.configuration.file.FileConfiguration;

public final class Config{
	private static FileConfiguration config;
	SeichiAssist plugin;
	public Config(SeichiAssist _plugin){
		plugin = _plugin;
		saveDefaultConfig();
		config = getConfig();
		loadGachaData();
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
		int num = config.getInt("num");
		for (int i=0; i < num; i++ ) {
			GachaData gachadata = new GachaData();
			gachadata.itemstack = config.getItemStack("item" + i);
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
/*
# ------------実行間隔のパラメータ---------------
# 最も採掘量の多いプレイヤーを称賛する間隔
no1playerinterval:
# 上のメッセージ送信基準破壊量(1分につき）
defaultmineamount: '3'


# -------------採掘速度関連のパラメータ---------------
# 採掘した量に応じた採掘速度変化（1分につき）
minutespeedamount: '0.01'

# ログイン人数に応じた採掘速度変化(１人につき）
onlineplayersamount: '0.5'





# ----------ガチャ券関連のパラメータ-----------------
# ガチャ券配布間隔ブロック数
presentinterval: '1000'


*/