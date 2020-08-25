package com.github.unchama.seichiassist.subsystems.expbottlestack

import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.ResourceScope
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.expbottlestack.bukkit.listeners.ExpBottleStackUsageController
import org.bukkit.entity.ThrownExpBottle

object System {
  def wired[
    F[_] : ConcurrentEffect,
    G[_] : SyncEffect : ContextCoercion[*[_], F]
  ](implicit effectEnvironment: EffectEnvironment): F[StatefulSubsystem[InternalState[F, G]]] = {
    import cats.implicits._

    for {
      managedExpBottleScope <- ResourceScope.create[F, G, ThrownExpBottle]
    } yield {
      implicit val scope: ResourceScope[F, G, ThrownExpBottle] = managedExpBottleScope

      StatefulSubsystem(
        listenersToBeRegistered = Seq(
          new ExpBottleStackUsageController[F, G]()
        ),
        commandsToBeRegistered = Map(),
        stateToExpose = InternalState[F, G](scope)
      )
    }
  }
}
