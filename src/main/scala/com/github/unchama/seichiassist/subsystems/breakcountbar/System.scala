package com.github.unchama.seichiassist.subsystems.breakcountbar

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.fs2.workaround.fs3.Fs3Topic
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.breakcountbar.application.{
  BreakCountBarVisibilityRepositoryDefinition,
  ExpBarSynchronizationRepositoryTemplate
}
import com.github.unchama.seichiassist.subsystems.breakcountbar.bukkit.CreateFreshBossBar
import com.github.unchama.seichiassist.subsystems.breakcountbar.domain.{
  BreakCountBarVisibility,
  BreakCountBarVisibilityPersistence
}
import com.github.unchama.seichiassist.subsystems.breakcountbar.infrastructure.JdbcBreakCountBarVisibilityPersistence
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

trait System[F[_], G[_], Player] extends Subsystem[F] {

  val api: BreakCountBarAPI[G, Player]

}

object System {

  import cats.implicits._

  private final val topicSubscriptionSize = 10

  def wired[G[_]: SyncEffect, F[_]: ConcurrentEffect: ContextCoercion[G, *[_]]: ErrorLogger](
    breakCountReadAPI: BreakCountReadAPI[F, G, Player]
  ): F[System[F, G, Player]] = {
    import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid.instance

    val persistence: BreakCountBarVisibilityPersistence[G] =
      new JdbcBreakCountBarVisibilityPersistence[G]

    for {
      topic <- Fs3Topic[F, Option[(Player, BreakCountBarVisibility)]]

      visibilityRepositoryHandles <- {
        ContextCoercion {
          BukkitRepositoryControls.createHandles(
            BreakCountBarVisibilityRepositoryDefinition.withContext[G, F, Player](
              persistence,
              stream => stream.map(Some.apply).through(topic.publish)
            )
          )
        }
      }

      visibilityValues = topic.subscribe(topicSubscriptionSize).mapFilter(identity)

      expBarSynchronizationRepositoryHandles <- {
        ContextCoercion {
          BukkitRepositoryControls.createHandles(
            RepositoryDefinition
              .Phased
              .TwoPhased(
                ExpBarSynchronizationRepositoryTemplate.initialization[G, F, Player](
                  breakCountReadAPI,
                  visibilityValues
                )(CreateFreshBossBar.in[G, F]),
                ExpBarSynchronizationRepositoryTemplate.finalization[G, F, Player]
              )
          )
        }
      }
    } yield {
      new System[F, G, Player] {
        override val api: BreakCountBarAPI[G, Player] = new BreakCountBarAPI[G, Player] {
          override val breakCountBarVisibility
            : KeyedDataRepository[Player, Ref[G, BreakCountBarVisibility]] =
            visibilityRepositoryHandles.repository
        }
        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] =
          Seq(visibilityRepositoryHandles, expBarSynchronizationRepositoryHandles).map(
            _.coerceFinalizationContextTo[F]
          )
      }
    }
  }
}
