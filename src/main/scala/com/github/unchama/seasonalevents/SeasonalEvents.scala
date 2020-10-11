package com.github.unchama.seasonalevents

import org.bukkit.plugin.Plugin

class SeasonalEvents(plugin: Plugin) {

  SeasonalEvents.plugin = plugin

  def onEnable(): Unit = {
    SeasonalEvents.config = new SeasonalEventsConfig(plugin)
    SeasonalEvents.config.loadConfig()
  }

  def onDisable(): Unit = {

  }
}

object SeasonalEvents {
  var plugin: Plugin = _
  var config: SeasonalEventsConfig = _
}