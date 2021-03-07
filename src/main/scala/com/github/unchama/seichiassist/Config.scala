package com.github.unchama.seichiassist

import com.github.unchama.bungeesemaphoreresponder.{RedisConnectionSettings, Configuration => BungeeSemaphoreResponderConfiguration}
import com.github.unchama.seichiassist.subsystems.autosave.application.{SystemConfiguration => AutoSaveConfiguration}
import com.github.unchama.seichiassist.subsystems.buildcount.application.{BuildExpMultiplier, Configuration => BuildCountConfiguration}
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.{Configuration => FastDiggingEffectConfiguration}
import org.bukkit.World
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.math.BigDecimal

object Config {
  def loadFrom(plugin: JavaPlugin): Config = { // config.ymlがない時にDefaultのファイルを生成
    plugin.saveDefaultConfig()
    plugin.reloadConfig()
    new Config(plugin.getConfig)
  }
}

final class Config private(val config: FileConfiguration) {
  def getFlyExp: Int = config.getString("flyexp").toInt

  // NOTE:
  //   config.getInt/config.getDoubleはnull値の場合0を返す
  //   getIntFailFast/getDoubleFailFastはNumberFormatExceptionを投げる
  private def getIntFailFast(path: String) = config.getString(path).toInt

  private def getDoubleFailFast(path: String) = config.getString(path).toDouble

  def getMinuteMineSpeed: Double = getDoubleFailFast("minutespeedamount")

  def getLoginPlayerMineSpeed: Double = getDoubleFailFast("onlineplayersamount")

  def getGachaPresentInterval: Int = getIntFailFast("presentinterval")

  def getDualBreaklevel: Int = getIntFailFast("dualbreaklevel")

  def getMultipleIDBlockBreaklevel: Int = getIntFailFast("multipleidblockbreaklevel")

  def getDropExplevel(i: Int): Double = getDoubleFailFast("dropexplevel" + i)

  def getPassivePortalInventorylevel: Int = getIntFailFast("passiveportalinventorylevel")

  def getDokodemoEnderlevel: Int = getIntFailFast("dokodemoenderlevel")

  def getMineStacklevel(i: Int): Int = getIntFailFast("minestacklevel" + i)

  def getDB: String = config.getString("db")

  def getTable: String = config.getString("table")

  def getID: String = config.getString("id")

  def getPW: String = config.getString("pw")

  def getURL: String = {
    val portComponent = {
      val port = config.getString("port", "")
      if (port.isEmpty) "" else ":" + port
    }
    val hostComponent = config.getString("host")

    s"jdbc:mysql://$hostComponent$portComponent"
  }

  //サーバー番号取得
  def getServerNum: Int = getIntFailFast("servernum")

  def getServerId: String = config.getString("server-id")

  def chunkSearchCommandBase: String = config.getString("chunk-search-command-base")

  //サブホーム最大数取得
  def getSubHomeMax: Int = getIntFailFast("subhomemax")

  def getDebugMode: Int = getIntFailFast("debugmode")

  def getMebiusDebug: Int = getIntFailFast("mebiusdebug")

  def rateGiganticToRingo: Int = getIntFailFast("rategigantictoringo")

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
   * @param world
   * @return
   */
  def getGridLimitPerWorld(world: String): Int = config.getString("GridLimitPerWorld." + world, config.getString("GridLimitDefault")).toInt

  def getTemplateKeepAmount: Int = getIntFailFast("GridTemplateKeepAmount")

  def getRoadY: Int = getIntFailFast("road_Y")

  def getRoadLength: Int = getIntFailFast("road_length")

  def getSpaceHeight: Int = getIntFailFast("space_height")

  def getRoadBlockID: Int = getIntFailFast("road_blockid")

  def getRoadBlockDamage: Int = getIntFailFast("road_blockdamage")

  def getContributeAddedMana: Int = getIntFailFast("contribute_added_mana")

  def getWorldSize: Int = getIntFailFast("world_size")

  def getGiganticFeverMinutes: Int = getIntFailFast("gigantic_fever_minutes")

  def getGiganticFeverDisplayTime: String = {
    val totalMinutes = getGiganticFeverMinutes
    (totalMinutes / 60) + "時間" + (totalMinutes % 60) + "分"
  }

  def getGiganticBerserkLimit: Int = getIntFailFast("GBLimit")

  /**
   * 各種URLを返します.
   *
   * @param typeName Url以下の項目名
   * @return 該当URL.ただし, typeNameが誤っていた場合は""を返します.
   */
  def getUrl(typeName: String): String = config.getString("Url." + typeName, "")

  def getFastDiggingEffectSystemConfiguration: FastDiggingEffectConfiguration = {
    new FastDiggingEffectConfiguration {
      override val amplifierPerBlockMined: Double = getDoubleFailFast("minutespeedamount")
      override val amplifierPerPlayerConnection: Double = getDoubleFailFast("onlineplayersamount")
    }
  }

  def getBungeeSemaphoreSystemConfiguration: BungeeSemaphoreResponderConfiguration = {
    val systemSettingsSection = config.getConfigurationSection("BungeeSemaphoreResponder")

    val _saveTimeoutDuration = {
      val timeoutMillis = systemSettingsSection.getInt("SaveTimeout", 55000)
      if (timeoutMillis < 0) {
        Duration.Inf
      } else {
        Duration(timeoutMillis, TimeUnit.MILLISECONDS)
      }
    }

    val _redis = {
      val redisSettingsSection = systemSettingsSection.getConfigurationSection("Redis")

      new RedisConnectionSettings {
        override val host: String = redisSettingsSection.getString("Host")
        override val port: Int = redisSettingsSection.getInt("Port")
        override val password: Option[String] =
          if (redisSettingsSection.contains("Password")) {
            Some(redisSettingsSection.getString("Password"))
          } else {
            None
          }
      }
    }

    new BungeeSemaphoreResponderConfiguration {
      override val redis: RedisConnectionSettings = _redis
      override val saveTimeoutDuration: Duration = _saveTimeoutDuration
    }
  }

  def getAutoSaveSystemConfiguration: AutoSaveConfiguration = new AutoSaveConfiguration {
    override val autoSaveEnabled: Boolean = config.getBoolean("AutoSave.Enable")
  }

  def buildCountConfiguration: BuildCountConfiguration = new BuildCountConfiguration {
    override val multipliers: BuildExpMultiplier = new BuildExpMultiplier {
      override val withBuildSkills: BigDecimal = BigDecimal(config.getString("BlockCountMag"))
      override val whenInSeichiWorld: BigDecimal = scala.math.BigDecimal.decimal(0.1)
    }
    override val oneMinuteBuildExpLimit: BuildExpAmount =
      BuildExpAmount(BigDecimal(config.getString("BuildNum1minLimit")))
  }
}