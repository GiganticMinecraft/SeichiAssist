package com.github.unchama.seichiassist.subsystems.mebius

import cats.effect.{IO, SyncIO, Timer}
import com.github.unchama.concurrent.{MinecraftServerThreadShift, RepeatingTaskContext}
import com.github.unchama.datarepository.bukkit.player.JoinToQuitPlayerDataRepository
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.PropertyModificationBukkitMessages
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.command.MebiusCommandExecutorProvider
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.gateway.BukkitMebiusSpeechGateway
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.listeners._
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.repository.{PeriodicMebiusSpeechRoutineFiberRepository, SpeechServiceRepository}
import com.github.unchama.seichiassist.subsystems.mebius.domain.message.PropertyModificationMessages
import com.github.unchama.seichiassist.subsystems.mebius.domain.speech.{MebiusSpeechBlockageState, MebiusSpeechGateway}
import com.github.unchama.seichiassist.subsystems.mebius.service.MebiusSpeechService
import org.bukkit.entity.Player

object System {
  def wired(implicit effectEnvironment: EffectEnvironment,
            timer: Timer[IO],
            repeatingTaskContext: RepeatingTaskContext,
            bukkitSyncIOShift: MinecraftServerThreadShift[IO]): Subsystem = {
    implicit val messages: PropertyModificationMessages = PropertyModificationBukkitMessages
    implicit val gatewayProvider: Player => MebiusSpeechGateway[SyncIO] = new BukkitMebiusSpeechGateway(_)
    implicit val getFreshSpeechBlockageState: SyncIO[MebiusSpeechBlockageState[SyncIO]] = SyncIO(new MebiusSpeechBlockageState[SyncIO])
    implicit val gatewayRepository: JoinToQuitPlayerDataRepository[MebiusSpeechService[SyncIO]] = new SpeechServiceRepository[SyncIO]

    val speechRoutineFiberRepository = new PeriodicMebiusSpeechRoutineFiberRepository()

    val listeners = Seq(
      new MebiusDropTrialListener,
      new MebiusInteractionResponder,
      new MebiusLevelUpTrialListener,
      new MebiusPlayerJoinGreeter[IO],
      new MebiusRenamePreventionListener,
      gatewayRepository, speechRoutineFiberRepository
    )

    val commands = Map(
      "mebius" -> new MebiusCommandExecutorProvider().executor
    )

    Subsystem(listeners, commands)
  }
}
