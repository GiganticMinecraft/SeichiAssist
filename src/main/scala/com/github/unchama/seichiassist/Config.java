package com.github.unchama.seichiassist;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Config {
    private final FileConfiguration config;

    private Config(FileConfiguration config) {
        this.config = config;
    }

    public static Config loadFrom(JavaPlugin plugin) {
        // config.ymlがない時にDefaultのファイルを生成
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        return new Config(plugin.getConfig());
    }

    public int getFlyExp() {
        return Integer.parseInt(config.getString("flyexp"));
    }

    // NOTE:
    //   config.getInt/config.getDoubleはnull値の場合0を返す
    //   getIntFailSafe/getDoubleFailSafeはNumberFormatExceptionを投げる
    private int getIntFailSafe(String path) {
        return Integer.parseInt(config.getString(path));
    }

    private double getDoubleFailSafe(String path) {
        return Double.parseDouble(config.getString(path));
    }

    public double getMinuteMineSpeed() {
        return getDoubleFailSafe("minutespeedamount");
    }

    public double getLoginPlayerMineSpeed() {
        return getDoubleFailSafe("onlineplayersamount");
    }

    public int getGachaPresentInterval() {
        return getIntFailSafe("presentinterval");
    }

    public int getDualBreaklevel() {
        return getIntFailSafe("dualbreaklevel");
    }

    public int getMultipleIDBlockBreaklevel() {
        return getIntFailSafe("multipleidblockbreaklevel");
    }

    public double getDropExplevel(final int i) {
        return getDoubleFailSafe("dropexplevel" + i);
    }

    public int getPassivePortalInventorylevel() {
        return getIntFailSafe("passiveportalinventorylevel");
    }

    public int getDokodemoEnderlevel() {
        return getIntFailSafe("dokodemoenderlevel");
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

    public String getServerId() {
        return config.getString("server-id");
    }

    public String chunkSearchCommandBase() {
        return config.getString("chunk-search-command-base");
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

    public boolean isAutoSaveEnabled() {
        return config.getBoolean("AutoSave.Enable");
    }
}
