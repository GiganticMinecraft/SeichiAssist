package com.github.unchama.seasonalevents

import com.github.unchama.seasonalevents.event.{Seizonsiki, Valentine}
import org.bukkit.plugin.Plugin

class SeasonalEvents(plugin: Plugin) {

  SeasonalEvents.plugin = plugin

  def onEnable(): Unit = {
    SeasonalEvents.config = new SeasonalEventsConfig(plugin)
    SeasonalEvents.config.loadConfig()

    new Seizonsiki(plugin)
    new Valentine(plugin)

    plugin.getLogger.info("SeasonalEvents is Enabled!")
  }

  def onDisable(): Unit = {
    plugin.getLogger.info("SeasonalEvents is Disabled!")
  }
}

object SeasonalEvents {
  var plugin: Plugin = _
  var config: SeasonalEventsConfig = _
}