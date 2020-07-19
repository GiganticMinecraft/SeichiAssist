package com.github.unchama.buildassist;

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
    private void saveDefaultConfig() {
        plugin.saveDefaultConfig();
    }

    //plugin.ymlファイルからの読み込み
    private FileConfiguration getConfig() {
        return plugin.getConfig();
    }


    public int getFlyExp() {
        return Integer.parseInt(config.getString("flyexp"));
    }

    //ブロックを並べるスキル開放LV
    public int getblocklineuplevel() {
        return Integer.parseInt(config.getString("blocklineup.level"));
    }

    //ブロックを並べるスキルのマナ消費倍率
    public double getblocklineupmana_mag() {
        return Double.parseDouble(config.getString("blocklineup.mana_mag"));
    }

    //ブロックを並べるスキルマインスタック優先開放LV
    public int getblocklineupMinestacklevel() {
        return Integer.parseInt(config.getString("blocklineup.minestack_level"));
    }

    public int getZoneSetSkillLevel() {
        return Integer.parseInt(config.getString("ZoneSetSkill.level"));
    }

    //スキルを使って並べた時のブロックカウント倍率
    public double getBlockCountMag() {
        return Double.parseDouble(config.getString("BlockCountMag"));
    }

    //MineStackブロック一括クラフト開放LV
    public int getMinestackBlockCraftlevel(int lv) {
        return Integer.parseInt(config.getString("minestack_BlockCraft.level" + lv));
    }

    //ブロック設置カウントの1分上限
    public int getBuildNum1minLimit() {
        return Integer.parseInt(config.getString("BuildNum1minLimit"));
    }

    //ブロック範囲設置スキルのマインスタック優先解放レベル
    public int getZoneskillMinestacklevel() {
        return Integer.parseInt(config.getString("ZoneSetSkill.minestack"));
    }

}