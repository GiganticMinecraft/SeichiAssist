package com.github.unchama.buildassist

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin

import scala.math.BigDecimal

class BuildAssistConfig(val plugin: Plugin) {
  private var config = plugin.getConfig

  saveDefaultConfig()

  def loadConfig(): Unit = {
    config = getConfig
  }

  /**
   * config.ymlがない時にDefaultのファイルを生成
   */
  private def saveDefaultConfig(): Unit = plugin.saveDefaultConfig()

  /**
   * config.ymlファイルからの読み込み
   */
  private def getConfig: FileConfiguration = plugin.getConfig

  def getFlyExpCostPerMinute: Int = config.getInt("flyexp")

  /**
   * ブロックを並べるスキル開放LV
   */
  def getUnlockLevelForLineUp: Int = config.getInt("blocklineup.level")

  /**
   * ブロックを並べるスキルのマナ消費倍率
   */
  def getLineUpManaCostMultiplier: Double = config.getDouble("blocklineup.mana_mag")

  /**
   * ブロックを並べるスキルマインスタック優先開放LV
   */
  def getLineFillPreferMineStackLevel: Int = config.getInt("blocklineup.minestack_level")

  def getUnlockLevelForZoneSet: Int = config.getInt("ZoneSetSkill.level")

  /**
   * MineStackブロック一括クラフト開放LV
   */
  def getUnlockLevelForMineStackBulkCraft(lv: Int): Int = config.getInt("minestack_BlockCraft.level" + lv)

  /**
   * ブロック設置カウントの1分上限
   */
  def getBuildingRatelimitPerMinute: BigDecimal = BigDecimal(config.getString("BuildNum1minLimit"))

  /**
   * ブロック範囲設置スキルのマインスタック優先解放レベル
   */
  def getZoneFillPreferMineStackLevel: Int = config.getInt("ZoneSetSkill.minestack")
}