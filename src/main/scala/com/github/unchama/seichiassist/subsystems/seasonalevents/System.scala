package com.github.unchama.seichiassist.subsystems.seasonalevents

import cats.Functor
import cats.effect.Clock
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.AnniversaryListener
import com.github.unchama.seichiassist.subsystems.seasonalevents.api.SeasonalEventsAPI
import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.ChristmasItemListener
import com.github.unchama.seichiassist.subsystems.seasonalevents.commands.EventCommand
import com.github.unchama.seichiassist.subsystems.seasonalevents.halloween.HalloweenItemListener
import com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin.LimitedLoginBonusGifter
import com.github.unchama.seichiassist.subsystems.seasonalevents.newyear.NewYearListener
import com.github.unchama.seichiassist.subsystems.seasonalevents.seizonsiki.SeizonsikiListener
import com.github.unchama.seichiassist.subsystems.seasonalevents.valentine.ValentineListener
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class System[F[_]](override val listeners: Seq[Listener],
                   override val managedFinalizers: Seq[PlayerDataFinalizer[F, Player]],
                   override val commands: Map[String, TabExecutor]) extends Subsystem[F] {

  def api[G[_] : Clock : Functor]: SeasonalEventsAPI[G] = SeasonalEventsAPI.withF[G]

}

object System {
  def wired[F[_]](instance: JavaPlugin): System[F] = {
    new System(
      listeners = Seq(
        AnniversaryListener,
        new ChristmasItemListener(instance),
        HalloweenItemListener,
        LimitedLoginBonusGifter,
        SeizonsikiListener,
        ValentineListener,
        NewYearListener,
      ),
      managedFinalizers = Nil,
      commands = Map(
        "event" -> EventCommand.executor
      )
    )
  }
}
