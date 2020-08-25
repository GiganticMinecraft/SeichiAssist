package com.github.unchama.seichiassist.subsystems.expbottlestack

import cats.effect.ConcurrentEffect
import com.github.unchama.generic.effect.ResourceScope
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.expbottlestack.bukkit.listeners.ExpBottleStackUsageController
import org.bukkit.entity.ThrownExpBottle

object System {
  def wired[F[_] : ConcurrentEffect](implicit effectEnvironment: EffectEnvironment): F[StatefulSubsystem[InternalState[F]]] = {
    import cats.implicits._

    for {
      managedExpBottleScope <- ResourceScope.create[F, F, ThrownExpBottle]
    } yield {
      implicit val scope: ResourceScope[F, F, ThrownExpBottle] = managedExpBottleScope

      StatefulSubsystem(
        listenersToBeRegistered = Seq(
          new ExpBottleStackUsageController[F]()
        ),
        commandsToBeRegistered = Map(),
        stateToExpose = InternalState[F](scope)
      )
    }
  }
}
