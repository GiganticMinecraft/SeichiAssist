package com.github.unchama.seasonalevents

import com.github.unchama.seasonalevents.seizonsiki.Seizonsiki
import com.github.unchama.seasonalevents.valentine.Valentine
import com.github.unchama.seichiassist.SeichiAssist

class SeasonalEvents(plugin: SeichiAssist) {
  implicit val config: SeasonalEventsConfig = new SeasonalEventsConfig(plugin)
  config.loadConfig()

  def onEnable(): Unit = {
    new Seizonsiki(plugin)
    new Valentine(plugin)

    plugin.getLogger.info("SeasonalEvents is Enabled!")
  }

  def onDisable(): Unit = {
    plugin.getLogger.info("SeasonalEvents is Disabled!")
  }
}
