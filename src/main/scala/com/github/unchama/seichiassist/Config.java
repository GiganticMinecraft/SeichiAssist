package com.github.unchama.seichiassist;

import com.github.unchama.seichiassist.util.TypeConverter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.OptionalInt;

public class Config {
    private static FileConfiguration config;
    private SeichiAssist plugin;

    //コンストラクタ
    Config(SeichiAssist _plugin) {
        plugin = _plugin;
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

    //config.ymlがない時にDefaultのファイルを生成
    public void saveDefaultConfig() {
        plugin.saveDefaultConfig();
    }

    //config.ymlファイルからの読み込み
    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public double getMinuteMineSpeed() {
        return config.getDouble("minutespeedamount");
    }

    public double getLoginPlayerMineSpeed() {
        return config.getDouble("onlineplayersamount");
    }

    public int getGachaPresentInterval() {
        return config.getInt("presentinterval");
    }

    public int getDefaultMineAmount() {
        return config.getInt("defaultmineamount");
    }

    public int getDualBreaklevel() {
        return config.getInt("dualbreaklevel");
    }

    public int getTrialBreaklevel() {
        return config.getInt("trialbreaklevel");
    }

    public int getExplosionlevel() {
        return config.getInt("explosionlevel");
    }

    public int getThunderStormlevel() {
        return config.getInt("thunderstormlevel");
    }

    public int getBlizzardlevel() {
        return config.getInt("blizzardlevel");
    }

    public int getMeteolevel() {
        return config.getInt("meteolevel");
    }

    public int getGravitylevel() {
        return config.getInt("gravitylevel");
    }

    public int getMultipleIDBlockBreaklevel() {
        return config.getInt("multipleidblockbreaklevel");
    }

    public double getDropExplevel(int i) {
        return config.getDoubleList("DropExp").get(i - 1);
    }

    public int getPassivePortalInventorylevel() {
        return TypeConverter.toInt(config.getString("passiveportalinventorylevel"));
    }

    public int getDokodemoEnderlevel() {
        return TypeConverter.toInt(config.getString("dokodemoenderlevel"));
    }

    public int getMineStacklevel(int i) {
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
        final int port = config.getInt("port");
        if (port != -1) {
            url += ":" + port;
        }
        return url;
    }

    public String getLvMessage(int i) {
        return config.getString("lv" + i + "message", "");
    }

    public String getTitle1(int i) {
        return config.getString("AchvA" + i, "");
    }

    public String getTitle2(int i) {
        return config.getString("AchvB" + i, "");
    }

    public String getTitle3(int i) {
        return config.getString("AchvC" + i, "");
    }

    //サーバー番号取得
    public int getServerNum() {
        return config.getInt("servernum");
    }

    //サブホーム最大数取得
    public int getSubHomeMax() {
        return TypeConverter.toInt(config.getString("subhomemax"));
    }

    public boolean isInDebugMode() {
        return config.getBoolean("debugmode");
    }

    @Deprecated
    public int getDebugMode() {
        return isInDebugMode() ? 1 : 0;
    }

    public boolean isInDebugMebiusMode() {
        return config.getBoolean("mebiusdebug");
    }

    @Deprecated
    public int getMebiusDebug() {
        return isInDebugMebiusMode() ? 1 : 0;
    }

    public int rateGiganticToRingo() {
        return config.getInt("ConvertRateToShina");
    }

    /**
     * 木の棒メニュー内のグリッド式保護メニューによる保護が許可されたワールドか
     *
     * @param player
     * @return
     */
    public boolean isGridProtectEnable(Player player) {
        List<String> worldlist = config.getStringList("GridProtectEnableWorld");

        return worldlist.stream()
                .anyMatch(name -> player.getWorld().getName().equalsIgnoreCase(name));
    }

    /**
     * ワールドごとのグリッド保護上限値を返却。該当の設定値がなければデフォ値を返却
     *
     * @param world
     * @return
     */
    public int getGridLimitPerWorld(String world) {
        return config.getInt("GridLimitPerWorld." + world, config.getInt("GridLimitDefault"));
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

    public int getPresentAmountForLimitedDurationCampaign(final int day) {
        final int ret;
        final int res = config.getInt("LimitedLoginEvent.Present." + day + ".Amount", -9999);
        if (res == -9999) {
            ret = 0;
        } else {
            ret = res;
        }
        return ret;
    }

    public OptionalInt getPresentItemIdForLimitedDurationCampaign(final int day) {
        final OptionalInt ret;
        final int res = config.getInt("LimitedLoginEvent.Present." + day + ".Item", -9999);
        if (res == -9999) {
            ret = OptionalInt.empty();
        } else {
            ret = OptionalInt.of(res);
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
        int minute = getGiganticFeverMinutes();

        int hours = minute / 60;
        int minutes = minute - 60 * hours;

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
    public String getUrl(String typeName) {
        return config.getString("Url." + typeName);
    }
}
