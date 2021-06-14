package com.github.unchama.buildassist

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin

import scala.math.BigDecimal

class BuildAssistConfig(val plugin: Plugin) {
  private var config = plugin.getConfig

  saveDefaultConfig()

  //コンフィグのロード
  def loadConfig(): Unit = {
    config = getConfig
  }

  //plugin.ymlがない時にDefaultのファイルを生成
  private def saveDefaultConfig(): Unit = plugin.saveDefaultConfig()

  //plugin.ymlファイルからの読み込み
  private def getConfig: FileConfiguration = plugin.getConfig

  def getFlyExp: Int = config.getString("flyexp").toInt

  //直列設置開放LV
  def getLineFillUnlockLevel: Int = config.getString("blocklineup.level").toInt

  //直列設置のマナ消費倍率
  def getLineFillManaCostMultiplier: Double = config.getString("blocklineup.mana_mag").toDouble

  //直列設置マインスタック優先開放LV
  def getLineFillFromMineStackUnlockLevel: Int = config.getString("blocklineup.minestack_level").toInt

  def getRectangleFillUnlockLevel: Int = config.getString("ZoneSetSkill.level").toInt

  //MineStackブロック一括クラフト開放LV
  def getMineStackBulkCraftUnlockLevel(lv: Int): Int = config.getString("minestack_BlockCraft.level" + lv).toInt

  //ブロック設置カウントの1分上限
  def getTrackingBlockCountLimitPerMinute: BigDecimal = BigDecimal(config.getString("BuildNum1minLimit"))

  //ブロック範囲設置スキルのマインスタック優先解放レベル
  def getRectangleFillFromMineStackUnlockLevel: Int = config.getString("ZoneSetSkill.minestack").toInt

}