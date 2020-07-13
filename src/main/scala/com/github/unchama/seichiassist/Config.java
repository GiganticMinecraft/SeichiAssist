package com.github.unchama.seichiassist;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public final class Config {
    private static FileConfiguration config;
    private final SeichiAssist plugin = SeichiAssist.instance();

    Config() {
        //config.ymlがない時にDefaultのファイルを生成
        plugin.saveDefaultConfig();
    }

    public void loadConfig() {
        config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfig();
    }

    // NOTE: config.getIntはnull値の場合0を返す
    private static int getIntFailSafe(String path) {
        return Integer.parseInt(config.getString(path));
    }

    public double getMinuteMineSpeed() {
        return Double.parseDouble(config.getString("minutespeedamount"));
    }

    public double getLoginPlayerMineSpeed() {
        return Double.parseDouble(config.getString("onlineplayersamount"));
    }

    public int getGachaPresentInterval() {
        return getIntFailSafe("presentinterval"));
    }

    public int getDualBreaklevel() {
        return getIntFailSafe("dualbreaklevel"));
    }

    public int getMultipleIDBlockBreaklevel() {
        return getIntFailSafe("multipleidblockbreaklevel"));
    }

    public double getDropExplevel(final int i) {
        return Double.parseDouble(config.getString("dropexplevel" + i, ""));
    }

    public int getPassivePortalInventorylevel() {
        return getIntFailSafe("passiveportalinventorylevel"));
    }

    public int getDokodemoEnderlevel() {
        return getIntFailSafe("dokodemoenderlevel"));
    }

    public int getMineStacklevel(final int i) {
        return getIntFailSafe("minestacklevel" + i);
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

        String port = config.getString("port", "");
        url += port.isEmpty() ? "" : ":" + port;

        return url;
    }

    public String getLvMessage(final int i) {
        return config.getString("lv" + i + "message", "");
    }

    //サーバー番号取得
    public int getServerNum() {
        return getIntFailSafe("servernum");
    }

    //サブホーム最大数取得
    public int getSubHomeMax() {
        return getIntFailSafe("subhomemax");
    }

    public int getDebugMode() {
        return getIntFailSafe("debugmode");
    }

    public int getMebiusDebug() {
        return getIntFailSafe("mebiusdebug");
    }

    public int rateGiganticToRingo() {
        return getIntFailSafe("rategigantictoringo");
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
        return Integer.parseInt(
            config.getString(
                "GridLimitPerWorld." + world,
                config.getString("GridLimitDefault")
            )
        );
    }

    public int getTemplateKeepAmount() {
        return getIntFailSafe("GridTemplateKeepAmount");
    }

    public int getRoadY() {
        return getIntFailSafe("road_Y");
    }

    public int getRoadLength() {
        return getIntFailSafe("road_length");
    }

    public int getSpaceHeight() {
        return getIntFailSafe("space_height");
    }

    public int getRoadBlockID() {
        return getIntFailSafe("road_blockid");
    }

    public int getRoadBlockDamage() {
        return getIntFailSafe("road_blockdamage");
    }

    public int getContributeAddedMana() {
        return getIntFailSafe("contribute_added_mana");
    }

    public String getLimitedLoginEventStart() {
        return config.getString("LimitedLoginEvent.EventStart");
    }

    public String getLimitedLoginEventEnd() {
        return config.getString("LimitedLoginEvent.EventEnd");
    }

    // getIntのnull値を0にする仕様を使っている
    public int getLimitedLoginEventItem(final int i) {
        return config.getInt("LimitedLoginEvent.DAY" + i + "_Item");
    }

    public int getLimitedLoginEventAmount(final int i) {
        return config.getInt("LimitedLoginEvent.DAY" + i + "_Amount");
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
        return getIntFailSafe("NewYearEvent.NewYearBagDropProbability");
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
        return getIntFailSafe("world_size");
    }

    public int getGiganticFeverMinutes() {
        return getIntFailSafe("gigantic_fever_minutes");
    }

    public String getGiganticFeverDisplayTime() {
        final int totalMinutes = getGiganticFeverMinutes();
        return (totalMinutes / 60) + "時間" + (totalMinutes % 60) + "分";
    }

    public int getGiganticBerserkLimit() {
        return getIntFailSafe("GBLimit");
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
