package com.github.unchama.seichiassist.subsystems.mebius

import cats.effect.{ContextShift, IO, Sync, SyncEffect, SyncIO, Timer}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.datarepository.bukkit.player.{
  BukkitRepositoryControls,
  PlayerDataRepository
}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.mebius.application.repository.{
  MebiusSpeechRoutineFiberRepositoryDefinitions,
  SpeechServiceRepositoryDefinitions
}
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.PropertyModificationBukkitMessages
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.command.MebiusCommandExecutorProvider
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.gateway.BukkitMebiusSpeechGateway
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.listeners._
import com.github.unchama.seichiassist.subsystems.mebius.domain.message.PropertyModificationMessages
import com.github.unchama.seichiassist.subsystems.mebius.domain.speech.{
  MebiusSpeechBlockageState,
  MebiusSpeechGateway
}
import com.github.unchama.seichiassist.subsystems.mebius.service.MebiusSpeechService
import com.github.unchama.seichiassist.subsystems.seasonalevents.api.SeasonalEventsAPI
import com.github.unchama.util.RandomEffect
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener

import scala.util.Random

object System {
  def wired[F[_]: Sync, G[_]: SeasonalEventsAPI: SyncEffect](
    implicit effectEnvironment: EffectEnvironment,
    timer: Timer[IO],
    repeatingTaskContext: RepeatingTaskContext,
    onMainThread: OnMinecraftServerThread[IO],
    ioShift: ContextShift[IO]
  ): SyncIO[Subsystem[F]] = {

    implicit val messages: PropertyModificationMessages = PropertyModificationBukkitMessages
    implicit val gatewayProvider: Player => MebiusSpeechGateway[SyncIO] =
      new BukkitMebiusSpeechGateway(_)
    implicit val getFreshSpeechBlockageState: SyncIO[MebiusSpeechBlockageState[SyncIO]] =
      SyncIO(new MebiusSpeechBlockageState[SyncIO])
    val seasonalEventsAPI = SeasonalEventsAPI[G]
    import seasonalEventsAPI.christmasEventsAPI

    implicit val randomEffect: RandomEffect[G] = RandomEffect.createFromRandom(Random)

    BukkitRepositoryControls
      .createHandles(
        RepositoryDefinition
          .Phased
          .TwoPhased(
            SpeechServiceRepositoryDefinitions.initialization[SyncIO, Player],
            SpeechServiceRepositoryDefinitions.finalization[SyncIO, Player]
          )
      )
      .flatMap { speechServiceRepositoryControls =>
        implicit val speechServiceRepository
          : PlayerDataRepository[MebiusSpeechService[SyncIO]] =
          speechServiceRepositoryControls.repository

        BukkitRepositoryControls
          .createHandles(
            RepositoryDefinition
              .Phased
              .TwoPhased(
                MebiusSpeechRoutineFiberRepositoryDefinitions.initialization[SyncIO],
                MebiusSpeechRoutineFiberRepositoryDefinitions.finalization[SyncIO, Player]
              )
          )
          .map { speechRoutineFiberRepositoryControls =>
            new Subsystem[F] {
              override val listeners: Seq[Listener] = Seq(
                new MebiusDropTrialListener[G],
                new MebiusInteractionResponder,
                new MebiusLevelUpTrialListener,
                new MebiusPlayerJoinGreeter[IO],
                new MebiusRenamePreventionListener
              )

              override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] =
                Seq(speechServiceRepositoryControls, speechRoutineFiberRepositoryControls).map(
                  _.coerceFinalizationContextTo[F]
                )

              override val commands: Map[String, TabExecutor] =
                Map("mebius" -> new MebiusCommandExecutorProvider().executor)
            }
          }
      }
  }
}
