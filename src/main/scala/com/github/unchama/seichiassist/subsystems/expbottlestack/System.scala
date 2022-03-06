package com.github.unchama.seichiassist.subsystems.expbottlestack

import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.ResourceScope
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.expbottlestack.bukkit.listeners.ExpBottleStackUsageController
import org.bukkit.entity.ThrownExpBottle
import org.bukkit.event.Listener

trait System[F[_], G[_], H[_]] extends Subsystem[H] {

  val managedBottleScope: ResourceScope[F, G, ThrownExpBottle]

}

object System {
  def wired[F[_]: ConcurrentEffect, G[_]: SyncEffect: ContextCoercion[*[_], F], H[_]](
    implicit effectEnvironment: EffectEnvironment
  ): F[System[F, G, H]] = {
    import cats.implicits._

    for {
      managedExpBottleScope <- ResourceScope.create[F, G, ThrownExpBottle]
    } yield {
      implicit val scope: ResourceScope[F, G, ThrownExpBottle] = managedExpBottleScope

      new System[F, G, H] {
        override val listeners: Seq[Listener] = Seq(new ExpBottleStackUsageController[F, G]())
        override val managedBottleScope: ResourceScope[F, G, ThrownExpBottle] = scope
      }
    }
  }
}
