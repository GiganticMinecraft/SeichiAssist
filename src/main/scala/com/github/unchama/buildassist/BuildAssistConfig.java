package com.github.unchama.buildassist;

import com.github.unchama.buildassist.util.TypeConverter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class BuildAssistConfig {
    private static FileConfiguration config;
    private final Plugin plugin;

    //コンストラクタ
    public BuildAssistConfig(final Plugin plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
    }

    //コンフィグのロード
    public void loadConfig() {
        config = getConfig();
    }

    //コンフィグのリロード
    public void reloadConfig() {
        plugin.reloadConfig();
        config = getConfig();
    }

    //コンフィグのセーブ
    public void saveConfig() {
        plugin.saveConfig();
    }

    //plugin.ymlがない時にDefaultのファイルを生成
    public void saveDefaultConfig() {
        plugin.saveDefaultConfig();
    }

    //plugin.ymlファイルからの読み込み
    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }


    public int getFlyExp() {
        return config.getInt("flyexp");
    }
/*
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
*/

    //ブロックを並べるスキル開放LV
    public int getblocklineuplevel() {
        return config.getInt("blocklineup.level");
    }

    //ブロックを並べるスキルのマナ消費倍率
    public double getblocklineupmana_mag() {
        return config.getDouble("blocklineup.mana_mag");
    }

    //ブロックを並べるスキルマインスタック優先開放LV
    public int getblocklineupMinestacklevel() {
        return TypeConverter.toInt(config.getString("blocklineup.minestack_level"));
    }

    public int getZoneSetSkillLevel() {
        return config.getInt("ZoneSetSkill.level");
    }

    //スキルを使って並べた時のブロックカウント倍率
    public double getBulkPlaceMultiplier() {
        return config.getDouble("BlockCountMag");
    }

    //MineStackブロック一括クラフト開放LV
    public int getMinestackBlockCraftlevel(int lv) {
        return config.getInt("minestack_BlockCraft.level" + lv);
    }

    //ブロック設置カウントの1分上限
    public int getCountLimitPerEveryMinute() {
        return config.getInt("BuildNum1minLimit");
    }

    //ブロック範囲設置スキルのマインスタック優先解放レベル
    public int getZoneskillMinestacklevel() {
        return config.getInt("ZoneSetSkill.minestack");
    }

}