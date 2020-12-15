package com.github.unchama.seichiassist.subsystems.seasonalevents

import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.AnniversaryListener
import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.ChristmasItemListener
import com.github.unchama.seichiassist.subsystems.seasonalevents.commands.EventCommand
import com.github.unchama.seichiassist.subsystems.seasonalevents.halloween.HalloweenItemListener
import com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin.LimitedLoginBonusGifter
import com.github.unchama.seichiassist.subsystems.seasonalevents.newyear.NewYearListener
import com.github.unchama.seichiassist.subsystems.seasonalevents.seizonsiki.SeizonsikiListener
import com.github.unchama.seichiassist.subsystems.seasonalevents.valentine.ValentineListener
import org.bukkit.plugin.java.JavaPlugin

object System {
  def wired[F[_]](instance: JavaPlugin): Subsystem[F] = {
    Subsystem(
      listenersToBeRegistered = Seq(
        AnniversaryListener,
        new ChristmasItemListener(instance),
        HalloweenItemListener,
        LimitedLoginBonusGifter,
        SeizonsikiListener,
        ValentineListener,
        NewYearListener,
      ),
      finalizersToBeManaged = Nil,
      commandsToBeRegistered = Map(
        "event" -> EventCommand.executor
      )
    )
  }
}
