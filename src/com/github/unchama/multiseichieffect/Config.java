package com.github.unchama.multiseichieffect;

import static com.github.unchama.multiseichieffect.Util.*;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {
	private FileConfiguration config;
	Config(FileConfiguration _config) {
		config = _config;
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
	public int getNo1PlayerInterval(){
		return toInt(config.getString("no1playerinterval"));
	}
	public int getDefaultMineAmount(){
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