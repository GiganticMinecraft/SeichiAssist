package com.github.unchama.seichiassist.subsystems.fastdiggingeffect

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, SyncEffect, Timer}
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.domain.playercount.GetConnectedPlayerCount
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.Configuration
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.process.{BreakCountEffectSynchronization, PlayerCountEffectSynchronization, SynchronizationProcess}
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.repository.{EffectListRepositoryDefinitions, SuppressionSettingsRepositoryDefinitions}
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.bukkit.actions.GrantBukkitFastDiggingEffect
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.actions.GrantFastDiggingEffect
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.{FastDiggingEffect, FastDiggingEffectList}
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.{FastDiggingEffectSuppressionState, FastDiggingEffectSuppressionStatePersistence}
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.infrastructure.JdbcFastDiggingEffectSuppressionStatePersistence
import fs2.concurrent.Topic
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener

import java.util.UUID
import scala.concurrent.duration.FiniteDuration

trait System[F[_], G[_], Player] extends Subsystem[G] {

  val effectApi: FastDiggingEffectApi[F, Player]

  val settingsApi: FastDiggingSettingsApi[F, Player]

}

object System {

  import cats.effect.implicits._
  import cats.implicits._
  import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid._

  def wired[
    G[_]
    : SyncEffect,
    F[_]
    : Timer
    : ConcurrentEffect
    : ContextCoercion[G, *[_]]
    : GetConnectedPlayerCount,
    H[_]
  ](implicit breakCountReadAPI: BreakCountReadAPI[F, H, Player], config: Configuration): F[System[F, F, Player]] = {

    implicit val suppressionStatePersistence: FastDiggingEffectSuppressionStatePersistence[G] =
      new JdbcFastDiggingEffectSuppressionStatePersistence[G]

    implicit val grantFastDiggingEffect: GrantFastDiggingEffect[F, Player] =
      new GrantBukkitFastDiggingEffect[F]

    val yieldSystem: F[System[F, F, Player]] = for {
      effectListTopic <- Topic[F, Option[(Player, FastDiggingEffectList)]](None)

      effectListRepositoryHandles <- {
        ContextCoercion {
          BukkitRepositoryControls
            .createTappingSinglePhasedRepositoryAndHandles(
              EffectListRepositoryDefinitions.initialization[F, G],
              EffectListRepositoryDefinitions.tappingAction[F, G, Player](effectListTopic),
              EffectListRepositoryDefinitions.finalization[F, G, UUID]
            )
        }
      }

      suppressionSettingsRepositoryHandles <- {
        ContextCoercion {
          BukkitRepositoryControls
            .createTwoPhasedRepositoryAndHandles(
              SuppressionSettingsRepositoryDefinitions.initialization(suppressionStatePersistence),
              SuppressionSettingsRepositoryDefinitions.finalization(suppressionStatePersistence)(_.getUniqueId)
            )
        }
      }

    } yield new System[F, F, Player] {
      override val effectApi: FastDiggingEffectApi[F, Player] = new FastDiggingEffectApi[F, Player] {
        override val currentEffect: KeyedDataRepository[Player, ReadOnlyRef[F, FastDiggingEffectList]] =
          effectListRepositoryHandles.repository.map { case (ref, _) =>
            ReadOnlyRef.fromAnySource(ContextCoercion(ref.readLatest))
          }

        override val effectClock: fs2.Stream[F, (Player, FastDiggingEffectList)] =
          effectListTopic.subscribe(1).mapFilter(identity)

        override def addEffect(effect: FastDiggingEffect, duration: FiniteDuration): Kleisli[F, Player, Unit] =
          Kleisli { player =>
            effectListRepositoryHandles
              .repository(player)._1
              .lockAndUpdate(_.appendEffect[F](effect, duration))
              .as(())
          }

      }
      override val settingsApi: FastDiggingSettingsApi[F, Player] = new FastDiggingSettingsApi[F, Player] {
        override val currentSuppressionSettings: KeyedDataRepository[Player, ReadOnlyRef[F, FastDiggingEffectSuppressionState]] =
          suppressionSettingsRepositoryHandles
            .repository
            .map(ref => ReadOnlyRef.fromRef(ref.mapK(ContextCoercion.asFunctionK)))
        override val toggleEffectSuppression: Kleisli[F, Player, Unit] = Kleisli { player =>
          ContextCoercion {
            suppressionSettingsRepositoryHandles.repository(player).update(_.nextState)
          }
        }
      }

      override val listeners: Seq[Listener] = Seq(
        effectListRepositoryHandles.initializer,
        suppressionSettingsRepositoryHandles.initializer
      )
      override val managedFinalizers: Seq[PlayerDataFinalizer[F, Player]] = Seq(
        effectListRepositoryHandles.finalizer.coerceContextTo[F],
        suppressionSettingsRepositoryHandles.finalizer.coerceContextTo[F]
      )
      override val commands: Map[String, TabExecutor] = Map.empty
    }

    yieldSystem.flatTap { system =>
      implicit val api: FastDiggingEffectApi[F, Player] = system.effectApi

      List(
        BreakCountEffectSynchronization.using[F, H, Player],
        PlayerCountEffectSynchronization.using[F, Player],
        SynchronizationProcess.using[F, Player](
          system.settingsApi.currentSuppressionSettings,
          system.effectApi.effectClock
        )
      ).traverse(_.compile.drain.start)
    }
  }
}
