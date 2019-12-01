package com.github.unchama.seichiassist;

import com.github.unchama.seichiassist.util.TypeConverter;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public final class Config {
    private static FileConfiguration config;
    private final SeichiAssist plugin = SeichiAssist.instance();

    Config() {
        saveDefaultConfig();
    }

    public void loadConfig() {
        config = getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = getConfig();
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    //config.ymlがない時にDefaultのファイルを生成
    private void saveDefaultConfig() {
        plugin.saveDefaultConfig();
    }

    //config.ymlファイルからの読み込み
    private FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public double getMinuteMineSpeed() {
        return TypeConverter.toDouble(config.getString("minutespeedamount"));
    }

    public double getLoginPlayerMineSpeed() {
        return TypeConverter.toDouble(config.getString("onlineplayersamount"));
    }

    public int getGachaPresentInterval() {
        return TypeConverter.toInt(config.getString("presentinterval"));
    }

    public int getDefaultMineAmount() {
        return TypeConverter.toInt(config.getString("defaultmineamount"));
    }

    public int getDualBreaklevel() {
        return TypeConverter.toInt(config.getString("dualbreaklevel"));
    }

    public int getTrialBreaklevel() {
        return TypeConverter.toInt(config.getString("trialbreaklevel"));
    }

    public int getExplosionlevel() {
        return TypeConverter.toInt(config.getString("explosionlevel"));
    }

    public int getThunderStormlevel() {
        return TypeConverter.toInt(config.getString("thunderstormlevel"));
    }

    public int getBlizzardlevel() {
        return TypeConverter.toInt(config.getString("blizzardlevel"));
    }

    public int getMeteolevel() {
        return TypeConverter.toInt(config.getString("meteolevel"));
    }

    public int getGravitylevel() {
        return TypeConverter.toInt(config.getString("gravitylevel"));
    }

    public int getMultipleIDBlockBreaklevel() {
        return TypeConverter.toInt(config.getString("multipleidblockbreaklevel"));
    }

    public double getDropExplevel(final int i) {
        return TypeConverter.toDouble(config.getString("dropexplevel" + i, ""));
    }

    public int getPassivePortalInventorylevel() {
        return TypeConverter.toInt(config.getString("passiveportalinventorylevel"));
    }

    public int getDokodemoEnderlevel() {
        return TypeConverter.toInt(config.getString("dokodemoenderlevel"));
    }

    public int getMineStacklevel(final int i) {
        return TypeConverter.toInt(config.getString("minestacklevel" + i, ""));
    }

    public String getDB() {
        return config.getString("db");
    }

    public String getTable() {
        return config.getString("table");
    }

    public String getID() {
        return config.getString("id");
    }

    public String getPW() {
        return config.getString("pw");
    }

    public String getURL() {
        String url = "jdbc:mysql://";
        url += config.getString("host");
        if (!config.getString("port").isEmpty()) {
            url += ":" + config.getString("port");
        }
        return url;
    }

    public String getLvMessage(final int i) {
        return config.getString("lv" + i + "message", "");
    }

    //サーバー番号取得
    public int getServerNum() {
        return TypeConverter.toInt(config.getString("servernum"));
    }

    //サブホーム最大数取得
    public int getSubHomeMax() {
        return TypeConverter.toInt(config.getString("subhomemax"));
    }

    public int getDebugMode() {
        return TypeConverter.toInt(config.getString("debugmode"));
    }

    public int getMebiusDebug() {
        return TypeConverter.toInt(config.getString("mebiusdebug"));
    }

    public int rateGiganticToRingo() {
        return TypeConverter.toInt(config.getString("rategigantictoringo"));
    }

    /**
     * 木の棒メニュー内のグリッド式保護メニューによる保護が許可されたワールドか
     * @deprecated 判定の対象はWorldなので意味論的におかしい
     * @param player
     * @return 許可されているならtrue、許可されていないならfalse
     */
    @Deprecated
    public boolean isGridProtectionEnabled(final Player player) {
        return isGridProtectionEnabled(player.getWorld());
    }

    /**
     * 木の棒メニュー内のグリッド式保護メニューによる保護が許可されたワールドか
     * @param world 対象のワールド
     * @return 許可されているならtrue、許可されていないならfalse
     */
    public boolean isGridProtectionEnabled(final World world) {
        return config.getStringList("GridProtectEnableWorld")
                .parallelStream()
                .anyMatch(name -> world.getName().equalsIgnoreCase(name));
    }

    /**
     * ワールドごとのグリッド保護上限値を返却。該当の設定値がなければデフォ値を返却
     *
     * @param world
     * @return
     */
    public int getGridLimitPerWorld(final String world) {
        return TypeConverter.toInt(config.getString("GridLimitPerWorld." + world, config.getString("GridLimitDefault")));
    }

    public int getTemplateKeepAmount() {
        return TypeConverter.toInt(config.getString("GridTemplateKeepAmount"));
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

    public String getLimitedLoginEventStart() {
        return config.getString("LimitedLoginEvent.EventStart");
    }

    public String getLimitedLoginEventEnd() {
        return config.getString("LimitedLoginEvent.EventEnd");
    }

    public String getLimitedLoginEventItem(final int i) {
        final String ret;
        if (config.getString("LimitedLoginEvent.DAY" + i + "_Item", "").isEmpty()) {
            ret = "0";
        } else {
            ret = config.getString("LimitedLoginEvent.DAY" + i + "_Item", "");
        }
        return ret;
    }

    public String getLimitedLoginEventAmount(final int i) {
        final String ret;
        if (config.getString("LimitedLoginEvent.DAY" + i + "_Amount", "").isEmpty()) {
            ret = "0";
        } else {
            ret = config.getString("LimitedLoginEvent.DAY" + i + "_Amount", "");
        }
        return ret;
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
        final int minute = getGiganticFeverMinutes();

        final int hours = minute / 60;
        final int minutes = minute - 60 * hours;

        return hours + "時間" + minutes + "分";
    }

    public int getGiganticBerserkLimit() {
        return config.getInt("GBLimit");
    }

    /**
     * 各種URLを返します.
     *
     * @param typeName Url以下の項目名
     * @return 該当URL.ただし, typeNameが誤っていた場合は""を返します.
     */
    public String getUrl(final String typeName) {
        return config.getString("Url." + typeName, "");
    }
}
