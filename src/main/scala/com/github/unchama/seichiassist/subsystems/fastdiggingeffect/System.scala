package com.github.unchama.seichiassist.subsystems.fastdiggingeffect

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, SyncEffect, SyncIO, Timer}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.fs2.workaround.fs3.Fs3Topic
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.actions.{
  GetConnectedPlayers,
  OnMinecraftServerThread,
  SendMinecraftMessage
}
import com.github.unchama.minecraft.bukkit.actions.SendBukkitMessage
import com.github.unchama.seichiassist.domain.actions.GetNetworkConnectionCount
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.Configuration
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.process.{
  BreakCountEffectSynchronization,
  EffectStatsNotification,
  PlayerCountEffectSynchronization,
  SynchronizationProcess
}
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.repository.{
  EffectListRepositoryDefinitions,
  EffectStatsSettingsRepositoryDefinition,
  SuppressionSettingsRepositoryDefinition
}
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.bukkit.actions.GrantBukkitFastDiggingEffect
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.actions.GrantFastDiggingEffect
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.{
  FastDiggingEffect,
  FastDiggingEffectList
}
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.{
  FastDiggingEffectSuppressionState,
  FastDiggingEffectSuppressionStatePersistence
}
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.stats.{
  EffectListDiff,
  FastDiggingEffectStatsSettings,
  FastDiggingEffectStatsSettingsPersistence
}
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.infrastructure.{
  JdbcFastDiggingEffectStatsSettingsPersistence,
  JdbcFastDiggingEffectSuppressionStatePersistence
}
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.Bukkit
import org.bukkit.entity.Player

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

  def wired[G[_]: SyncEffect, F[
    _
  ]: OnMinecraftServerThread: Timer: ConcurrentEffect: ErrorLogger: ContextCoercion[
    G,
    *[_]
  ]: GetConnectedPlayers[*[_], Player]: GetNetworkConnectionCount, H[_]](
    implicit breakCountReadAPI: BreakCountReadAPI[F, H, Player],
    config: Configuration
  ): F[System[F, F, Player]] = {

    val settingsPersistence: FastDiggingEffectStatsSettingsPersistence[G] =
      new JdbcFastDiggingEffectStatsSettingsPersistence[G]

    val suppressionStatePersistence: FastDiggingEffectSuppressionStatePersistence[G] =
      new JdbcFastDiggingEffectSuppressionStatePersistence[G]

    implicit val FSendMinecraftMessage: SendMinecraftMessage[F, Player] =
      new SendBukkitMessage[F]

    implicit val grantFastDiggingEffect: GrantFastDiggingEffect[F, Player] =
      new GrantBukkitFastDiggingEffect[F]

    val yieldSystem: F[System[F, F, Player]] = for {
      effectListTopic <- Fs3Topic[F, Option[(Player, FastDiggingEffectList)]]
      effectListDiffTopic <- Fs3Topic[F, Option[
        (Player, (EffectListDiff, FastDiggingEffectStatsSettings))
      ]]

      effectListRepositoryHandles <- {
        ContextCoercion {
          BukkitRepositoryControls.createHandles(
            RepositoryDefinition
              .Phased
              .SinglePhased(
                EffectListRepositoryDefinitions.initialization[F, G],
                EffectListRepositoryDefinitions.tappingAction[F, G, Player](effectListTopic),
                EffectListRepositoryDefinitions.finalization[F, G, UUID]
              )
          )
        }
      }

      suppressionSettingsRepositoryHandles <- {
        ContextCoercion {
          BukkitRepositoryControls.createHandles(
            SuppressionSettingsRepositoryDefinition.withContext(suppressionStatePersistence)
          )
        }
      }

      statsSettingsRepositoryHandles <- {
        ContextCoercion {
          BukkitRepositoryControls.createHandles(
            EffectStatsSettingsRepositoryDefinition.withContext[F, G, Player](
              settingsPersistence,
              stream => stream.map(Some.apply).through(effectListDiffTopic.publish),
              effectListTopic.subscribe(1).mapFilter(identity)
            )
          )
        }
      }

      _ <-
        StreamExtra
          .compileToRestartingStream[F, Unit]("[FastDiggingEffect/EffectStatsNotification]") {
            EffectStatsNotification.using[F, Player](
              effectListDiffTopic.subscribe(1).mapFilter(identity)
            )
          }
          .start

    } yield new System[F, F, Player] {
      override val effectApi: FastDiggingEffectApi[F, Player] =
        new FastDiggingEffectApi[F, Player] {
          override val currentEffect
            : KeyedDataRepository[Player, ReadOnlyRef[F, FastDiggingEffectList]] =
            effectListRepositoryHandles.repository.map {
              case (ref, _) =>
                ReadOnlyRef.fromAnySource(ContextCoercion(ref.readLatest))
            }

          override val effectClock: fs2.Stream[F, (Player, FastDiggingEffectList)] =
            effectListTopic.subscribe(1).mapFilter(identity)

          override def addEffect(
            effect: FastDiggingEffect,
            duration: FiniteDuration
          ): Kleisli[F, Player, Unit] =
            Kleisli { player =>
              effectListRepositoryHandles
                .repository
                .lift(player)
                .traverse { pair =>
                  pair._1.lockAndUpdate(_.appendEffect[F](effect, duration)).as(())
                }
                .as(())
            }

          override def addEffectToAllPlayers(
            effect: FastDiggingEffect,
            duration: FiniteDuration
          ): F[Unit] = {
            import cats.implicits._

            import scala.concurrent.duration._
            import scala.jdk.CollectionConverters._

            for {
              players <- OnMinecraftServerThread[F].runAction(
                SyncIO(Bukkit.getOnlinePlayers.asScala.toList)
              )
              _ <- players.traverse(addEffect(effect, 1.hour).run)
            } yield ()
          }.as(())

        }
      override val settingsApi: FastDiggingSettingsApi[F, Player] =
        new FastDiggingSettingsApi[F, Player] {
          override val currentSuppressionSettings
            : KeyedDataRepository[Player, ReadOnlyRef[F, FastDiggingEffectSuppressionState]] =
            suppressionSettingsRepositoryHandles
              .repository
              .map(ref => ReadOnlyRef.fromRef(ref.mapK(ContextCoercion.asFunctionK)))
          override val toggleEffectSuppression
            : Kleisli[F, Player, FastDiggingEffectSuppressionState] = Kleisli { player =>
            ContextCoercion {
              suppressionSettingsRepositoryHandles.repository(player).updateAndGet(_.nextState)
            }
          }
          override val toggleStatsSettings: Kleisli[F, Player, FastDiggingEffectStatsSettings] =
            Kleisli { player =>
              ContextCoercion {
                statsSettingsRepositoryHandles.repository(player)._1.updateAndGet(_.nextValue)
              }
            }
          override val currentStatsSettings
            : KeyedDataRepository[Player, ReadOnlyRef[F, FastDiggingEffectStatsSettings]] =
            statsSettingsRepositoryHandles
              .repository
              .map(pair => ReadOnlyRef.fromRef(pair._1.mapK(ContextCoercion.asFunctionK)))
        }

      override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Seq(
        effectListRepositoryHandles,
        suppressionSettingsRepositoryHandles,
        statsSettingsRepositoryHandles
      ).map(_.coerceFinalizationContextTo[F])
    }

    yieldSystem.flatTap { system =>
      implicit val api: FastDiggingEffectApi[F, Player] = system.effectApi

      List(
        "BreakCountEffectSynchronization" -> BreakCountEffectSynchronization
          .using[F, H, Player],
        "PlayerCountEffectSynchronization" -> PlayerCountEffectSynchronization.using[F, Player],
        "SynchronizationProcess" -> SynchronizationProcess.using[F, Player](
          system.settingsApi.currentSuppressionSettings,
          system.effectApi.effectClock
        )
      ).traverse {
        case (str, stream) =>
          StreamExtra.compileToRestartingStream(s"[FastDiggingEffect/$str]")(stream).start
      }
    }
  }
}
