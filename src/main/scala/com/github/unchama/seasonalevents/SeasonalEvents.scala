package com.github.unchama.seasonalevents

import com.github.unchama.seasonalevents.commands.EventCommand
import com.github.unchama.seasonalevents.halloween.HalloweenItemListener
import com.github.unchama.seasonalevents.newyear.NewYearListener
import com.github.unchama.seasonalevents.seizonsiki.SeizonsikiListener
import com.github.unchama.seasonalevents.valentine.ValentineListener
import com.github.unchama.seichiassist.SeichiAssist

class SeasonalEvents(instance: SeichiAssist) {
  def onEnable(): Unit = {
    List(
      HalloweenItemListener,
      SeizonsikiListener,
      ValentineListener,
      new NewYearListener(instance),
    ).foreach(
      instance.getServer.getPluginManager.registerEvents(_, instance)
    )

    instance.getCommand("event").setExecutor(EventCommand.executor)

    instance.getLogger.info("SeasonalEvents is Enabled!")
  }

  def onDisable(): Unit = {
    instance.getLogger.info("SeasonalEvents is Disabled!")
  }
}
