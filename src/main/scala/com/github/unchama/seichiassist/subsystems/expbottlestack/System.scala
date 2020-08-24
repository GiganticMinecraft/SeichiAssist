package com.github.unchama.seichiassist.subsystems.expbottlestack

import cats.effect.{Concurrent, IO}
import com.github.unchama.generic.effect.ResourceScope
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.expbottlestack.bukkit.listeners.ExpBottleStackUsageController
import org.bukkit.entity.ThrownExpBottle

object System {
  def wired(implicit effectEnvironment: EffectEnvironment,
            ioConcurrent: Concurrent[IO]): IO[StatefulSubsystem[InternalState[IO]]] = {
    for {
      managedExpBottleScope <- ResourceScope.create[IO, ThrownExpBottle]
    } yield {
      implicit val scope: ResourceScope[IO, ThrownExpBottle] = managedExpBottleScope

      StatefulSubsystem(
        listenersToBeRegistered = Seq(
          new ExpBottleStackUsageController()
        ),
        commandsToBeRegistered = Map(),
        stateToExpose = InternalState[IO](scope)
      )
    }
  }
}
