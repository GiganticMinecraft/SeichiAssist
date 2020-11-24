package com.github.unchama.seichiassist

import org.bukkit.World
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin


object Config {
  def loadFrom(plugin: JavaPlugin): Config = {
    // config.ymlがない時にDefaultのファイルを生成
    plugin.saveDefaultConfig()
    plugin.reloadConfig()
    new Config(plugin.getConfig)
  }
}

class Config private (val config: FileConfiguration) {
  def getFlyExp: Int = config.getString("flyexp").toInt

  // NOTE:
  //   config.getInt/config.getDoubleはnull値の場合0を返す
  //   getIntFailSafe/getDoubleFailSafeはNumberFormatExceptionを投げる
  private def getIntFailSafe(path: String): Int = config.getString(path).toInt

  private def getDoubleFailSafe(path: String): Double = config.getString(path).toDouble

  def getMinuteMineSpeed: Double = getDoubleFailSafe("minutespeedamount")

  def getLoginPlayerMineSpeed: Double = getDoubleFailSafe("onlineplayersamount")

  def getGachaPresentInterval: Int = getIntFailSafe("presentinterval")

  def getDualBreaklevel: Int = getIntFailSafe("dualbreaklevel")

  def getMultipleIDBlockBreaklevel: Int = getIntFailSafe("multipleidblockbreaklevel")

  def getDropExplevel(i: Int): Double = getDoubleFailSafe("dropexplevel" + i)

  def getPassivePortalInventorylevel: Int = getIntFailSafe("passiveportalinventorylevel")

  def getDokodemoEnderlevel: Int = getIntFailSafe("dokodemoenderlevel")

  def getMineStacklevel(i: Int): Int = getIntFailSafe("minestacklevel" + i)

  def getDB: String = config.getString("db")

  def getTable: String = config.getString("table")

  def getID: String = config.getString("id")

  def getPW: String = config.getString("pw")

  def getURL: String = {
    var url = "jdbc:mysql://"
    url += config.getString("host")
    val port = config.getString("port", "")
    url = url.appendedAll(if (port.isEmpty) "" else ":" + port)
    url
  }

  def getLvMessage(i: Int): String = config.getString("lv" + i + "message", "")

  //サーバー番号取得
  def getServerNum: Int = getIntFailSafe("servernum")

  def getServerId: String = config.getString("server-id")

  def chunkSearchCommandBase: String = config.getString("chunk-search-command-base")

  //サブホーム最大数取得
  def getSubHomeMax: Int = getIntFailSafe("subhomemax")

  def getDebugMode: Int = getIntFailSafe("debugmode")

  def getMebiusDebug: Int = getIntFailSafe("mebiusdebug")

  def rateGiganticToRingo: Int = getIntFailSafe("rategigantictoringo")

  /**
   * 木の棒メニュー内のグリッド式保護メニューによる保護が許可されたワールドか
   *
   * @param world 対象のワールド
   * @return 許可されているならtrue、許可されていないならfalse
   */
  def isGridProtectionEnabled(world: World): Boolean = config.getStringList("GridProtectEnableWorld").parallelStream.anyMatch((name: String) => world.getName.equalsIgnoreCase(name))

  /**
   * ワールドごとのグリッド保護上限値を返却。該当の設定値がなければデフォ値を返却
   *
   * @param world 対象のワールド
   * @return
   */
  def getGridLimitPerWorld(world: String): Int = config.getString("GridLimitPerWorld." + world, config.getString("GridLimitDefault")).toInt

  def getTemplateKeepAmount: Int = getIntFailSafe("GridTemplateKeepAmount")

  def getRoadY: Int = getIntFailSafe("road_Y")

  def getRoadLength: Int = getIntFailSafe("road_length")

  def getSpaceHeight: Int = getIntFailSafe("space_height")

  def getRoadBlockID: Int = getIntFailSafe("road_blockid")

  def getRoadBlockDamage: Int = getIntFailSafe("road_blockdamage")

  def getContributeAddedMana: Int = getIntFailSafe("contribute_added_mana")

  def getLimitedLoginEventStart: String = config.getString("LimitedLoginEvent.EventStart")

  def getLimitedLoginEventEnd: String = config.getString("LimitedLoginEvent.EventEnd")

  // getIntのnull値を0にする仕様を使っている
  def getLimitedLoginEventItem(i: Int): Int = config.getInt("LimitedLoginEvent.DAY" + i + "_Item")

  def getLimitedLoginEventAmount(i: Int): Int = config.getInt("LimitedLoginEvent.DAY" + i + "_Amount")

  def getGivingNewYearSobaDay: String = config.getString("NewYearEvent.GivingNewYearSobaDay")

  def getNewYearSobaYear: String = config.getString("NewYearEvent.NewYearSobaYear")

  def getDropNewYearBagStartDay: String = config.getString("NewYearEvent.DropNewYearBagStartDay")

  def getDropNewYearBagEndDay: String = config.getString("NewYearEvent.DropNewYearBagEndDay")

  def getNewYearDropProbability: Int = getIntFailSafe("NewYearEvent.NewYearBagDropProbability")

  def getNewYear: String = config.getString("NewYearEvent.NewYear")

  def getNewYearAppleStartDay: String = config.getString("NewYearEvent.NewYearAppleStartDay")

  def getNewYearAppleEndDay: String = config.getString("NewYearEvent.NewYearAppleEndDay")

  def getWorldSize: Int = getIntFailSafe("world_size")

  def getGiganticFeverMinutes: Int = getIntFailSafe("gigantic_fever_minutes")

  def getGiganticFeverDisplayTime: String = {
    val totalMinutes = getGiganticFeverMinutes
    (totalMinutes / 60) + "時間" + (totalMinutes % 60) + "分"
  }

  def getGiganticBerserkLimit: Int = getIntFailSafe("GBLimit")

  /**
   * 各種URLを返します.
   *
   * @param typeName Url以下の項目名
   * @return 該当URL.ただし, typeNameが誤っていた場合は""を返します.
   */
  def getUrl(typeName: String): String = config.getString("Url." + typeName, "")
}
