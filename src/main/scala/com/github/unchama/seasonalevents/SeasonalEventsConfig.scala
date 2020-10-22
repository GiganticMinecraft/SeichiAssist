package com.github.unchama.seasonalevents

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin

class SeasonalEventsConfig(private val plugin: Plugin) {
  private var config: FileConfiguration = _

  saveDefaultConfig()

  // コンフィグのロード
  def loadConfig(): Unit = {
    config = getConfig
  }

  // コンフィグのリロード
  def reloadConfig(): Unit = {
    plugin.reloadConfig()
    config = getConfig
  }

  // コンフィグのセーブ
  def saveConfig(): Unit = plugin.saveConfig()

  // plugin.ymlがない時にDefaultのファイルを生成
  def saveDefaultConfig(): Unit = plugin.saveDefaultConfig()

  // plugin.ymlファイルからの読み込み
  def getConfig: FileConfiguration = plugin.getConfig

  // TODO Optionに包む？

  def getDropRate: Double = config.getString("dropper").toDouble

  def getWikiAddr: String = config.getString("wiki")

  /**
   * イベントドロップ終了日を指定します。(西暦4桁-月2桁-日付2桁)
   *
   * @return イベントドロップ終了日 (西暦4桁-月2桁-日付2桁)
   */
  def getDropFinishDay: String = config.getString("DropFinishDay")

  /**
   * イベント終了日を取得します。(西暦4桁-月2桁-日付2桁)
   *
   * @return イベント終了日 (西暦4桁-月2桁-日付2桁)
   */
  def getEventFinishDay: String = config.getString("EventFinishDay")

  /**
   * イベントドロップ終了日(表示用)を取得します。(西暦4桁/月2桁/日付2桁)
   *
   * @return イベントドロップ終了日(表示用) (西暦4桁/月2桁/日付2桁)
   */
  def getDropFinishDayDisp: String = config.getString("DropFinishDayDisp")

  /**
   * イベント終了日(表示用)を取得します。(西暦4桁/月2桁/日付2桁)
   *
   * @return イベント終了日(表示用) (西暦4桁/月2桁/日付2桁)
   */
  def getEventFinishDayDisp: String = config.getString("EventFinishDayDisp")
}
