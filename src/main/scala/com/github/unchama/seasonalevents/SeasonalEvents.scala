package com.github.unchama.seasonalevents

import com.github.unchama.seasonalevents.seizonsiki.SeizonsikiListener
import com.github.unchama.seasonalevents.valentine.ValentineListener
import com.github.unchama.seichiassist.SeichiAssist

class SeasonalEvents(plugin: SeichiAssist) {
  implicit val config: SeasonalEventsConfig = new SeasonalEventsConfig(plugin)
  config.loadConfig()

  def onEnable(): Unit = {
    plugin.getServer.getPluginManager.registerEvents(new SeizonsikiListener(), plugin)
    plugin.getServer.getPluginManager.registerEvents(new ValentineListener(), plugin)

    plugin.getLogger.info("SeasonalEvents is Enabled!")
  }

  def onDisable(): Unit = {
    plugin.getLogger.info("SeasonalEvents is Disabled!")
  }
}
