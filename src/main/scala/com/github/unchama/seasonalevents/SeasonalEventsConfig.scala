package com.github.unchama.seasonalevents

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin

class SeasonalEventsConfig(private val plugin: Plugin) {
  private var config: FileConfiguration = _

  // コンフィグのロード
  def loadConfig(): Unit = {
    config = plugin.getConfig
  }

  def itemDropRate: Double = Option(config.getInt("SeasonalEvents.ItemDropRate"))
    .filter(rate => 0 <= rate && rate <= 100)
    .getOrElse(30)
    .toDouble

  def blogArticleUrl: String = Option(config.getString("SeasonalEvents.HPUrl"))
    .filter(_.startsWith("https://www.seichi.network/post/"))
    .getOrElse("https://www.seichi.network/blog/categories/%E3%82%A4%E3%83%99%E3%83%B3%E3%83%88%E6%83%85%E5%A0%B1")
}
