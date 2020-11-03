package com.github.unchama.seasonalevents

import com.github.unchama.seasonalevents.commands.EventCommand
import com.github.unchama.seasonalevents.halloween.HalloweenItemListener
import com.github.unchama.seasonalevents.seizonsiki.SeizonsikiListener
import com.github.unchama.seasonalevents.valentine.ValentineListener
import com.github.unchama.seichiassist.SeichiAssist

class SeasonalEvents(plugin: SeichiAssist) {
  def onEnable(): Unit = {
    List(
      new HalloweenItemListener(),
      new SeizonsikiListener(),
      new ValentineListener()
    ).foreach(
      plugin.getServer.getPluginManager.registerEvents(_, plugin)
    )

    plugin.getCommand("event").setExecutor(EventCommand.executor)

    plugin.getLogger.info("SeasonalEvents is Enabled!")
  }

  def onDisable(): Unit = {
    plugin.getLogger.info("SeasonalEvents is Disabled!")
  }
}
