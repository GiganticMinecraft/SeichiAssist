package com.github.unchama.seasonalevents

import com.github.unchama.seasonalevents.seizonsiki.Seizonsiki
import com.github.unchama.seasonalevents.valentine.Valentine
import com.github.unchama.seichiassist.SeichiAssist

class SeasonalEvents(plugin: SeichiAssist) {

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
  var config: SeasonalEventsConfig = _
}