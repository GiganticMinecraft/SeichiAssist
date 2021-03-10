package com.github.unchama.seichiassist.subsystems.breakcountbar

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.breakcountbar.application.{BreakCountBarVisibilityRepositoryTemplate, ExpBarSynchronizationRepositoryTemplate}
import com.github.unchama.seichiassist.subsystems.breakcountbar.bukkit.CreateFreshBossBar
import com.github.unchama.seichiassist.subsystems.breakcountbar.domain.{BreakCountBarVisibility, BreakCountBarVisibilityPersistence}
import com.github.unchama.seichiassist.subsystems.breakcountbar.infrastructure.JdbcBreakCountBarVisibilityPersistence
import fs2.concurrent.Topic
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener

trait System[F[_], G[_], Player] extends Subsystem[F] {

  val api: BreakCountBarAPI[G, Player]

}

object System {

  import cats.implicits._

  private final val topicSubscriptionSize = 10

  def wired[
    G[_] : SyncEffect,
    F[_] : ConcurrentEffect : ContextCoercion[G, *[_]],
  ](breakCountReadAPI: BreakCountReadAPI[F, G, Player]): F[System[F, G, Player]] = {
    import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid.instance

    val persistence: BreakCountBarVisibilityPersistence[G] =
      new JdbcBreakCountBarVisibilityPersistence[G]

    for {
      topic <- Topic[F, Option[(Player, BreakCountBarVisibility)]](None)

      visibilityRepositoryHandles <- {
        val initialization =
          BreakCountBarVisibilityRepositoryTemplate
            .initialization[G, F, Player](persistence, topic)

        val finalization =
          BreakCountBarVisibilityRepositoryTemplate
            .finalization[G, Player](persistence)(_.getUniqueId)

        ContextCoercion {
          BukkitRepositoryControls.createTwoPhasedRepositoryAndHandles(initialization, finalization)
        }
      }

      visibilityValues = topic.subscribe(topicSubscriptionSize).mapFilter(identity)

      expBarSynchronizationRepositoryHandles <- {
        val initialization =
          ExpBarSynchronizationRepositoryTemplate
            .initialization[G, F, Player](
              breakCountReadAPI.seichiAmountUpdates,
              visibilityValues
            )(CreateFreshBossBar.in[G, F])

        val finalization =
          ExpBarSynchronizationRepositoryTemplate
            .finalization[G, F, Player]

        ContextCoercion {
          BukkitRepositoryControls.createTwoPhasedRepositoryAndHandles(initialization, finalization)
        }
      }
    } yield {
      new System[F, G, Player] {
        override val api: BreakCountBarAPI[G, Player] = new BreakCountBarAPI[G, Player] {
          override val breakCountBarVisibility: KeyedDataRepository[Player, Ref[G, BreakCountBarVisibility]] =
            visibilityRepositoryHandles.repository
        }
        override val listeners: Seq[Listener] = Vector(
          visibilityRepositoryHandles.initializer,
          expBarSynchronizationRepositoryHandles.initializer
        )
        override val managedFinalizers: Seq[PlayerDataFinalizer[F, Player]] = Vector(
          visibilityRepositoryHandles.finalizer,
          expBarSynchronizationRepositoryHandles.finalizer
        ).map(_.coerceContextTo[F])
        override val commands: Map[String, TabExecutor] = Map()
      }
    }
  }
}
