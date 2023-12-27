package com.github.unchama.seichiassist.subsystems.elevator

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.elevator.application.actions.FindTeleportLocation
import com.github.unchama.seichiassist.subsystems.elevator.bukkit.actions.BukkitFindTeleportLocation
import com.github.unchama.seichiassist.subsystems.elevator.bukkit.listeners.ElevatorEventsListener
import org.bukkit.Location
import org.bukkit.event.Listener

object System {

  def wired[F[_]: ConcurrentEffect]: Subsystem[F] = {
    implicit val findTeleportLocation: FindTeleportLocation[F, Location] =
      new BukkitFindTeleportLocation[F]

    new Subsystem[F] {
      override val listeners: Seq[Listener] = Seq(new ElevatorEventsListener[F])
    }
  }

}
