package com.github.unchama.seichiassist.mebius

import cats.effect.{IO, Timer}
import com.github.unchama.concurrent.{BukkitSyncIOShift, RepeatingTaskContext}
import com.github.unchama.playerdatarepository.JoinToQuitPlayerDataRepository
import com.github.unchama.seichiassist.SubsystemEntryPoints
import com.github.unchama.seichiassist.domain.unsafe.SeichiAssistEffectEnvironment
import com.github.unchama.seichiassist.mebius.bukkit.PropertyModificationBukkitMessages
import com.github.unchama.seichiassist.mebius.bukkit.command.MebiusCommandExecutorProvider
import com.github.unchama.seichiassist.mebius.bukkit.gateway.BukkitMebiusSpeechGateway
import com.github.unchama.seichiassist.mebius.bukkit.listeners._
import com.github.unchama.seichiassist.mebius.bukkit.repository.{PeriodicMebiusSpeechRoutineFiberRepository, SpeechServiceRepository}
import com.github.unchama.seichiassist.mebius.domain.message.PropertyModificationMessages
import com.github.unchama.seichiassist.mebius.domain.speech.{MebiusSpeechBlockageState, MebiusSpeechGateway}
import com.github.unchama.seichiassist.mebius.service.MebiusSpeechService
import org.bukkit.entity.Player

object EntryPoints {
  def wired(implicit effectEnvironment: SeichiAssistEffectEnvironment,
            timer: Timer[IO],
            repeatingTaskContext: RepeatingTaskContext,
            bukkitSyncIOShift: BukkitSyncIOShift): SubsystemEntryPoints = {
    implicit val messages: PropertyModificationMessages = PropertyModificationBukkitMessages
    implicit val gatewayProvider: Player => MebiusSpeechGateway[IO] = new BukkitMebiusSpeechGateway(_)
    implicit val getFreshSpeechBlockageState: IO[MebiusSpeechBlockageState[IO]] = IO(new MebiusSpeechBlockageState[IO])
    implicit val gatewayRepository: JoinToQuitPlayerDataRepository[MebiusSpeechService[IO]] = new SpeechServiceRepository[IO]

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

    SubsystemEntryPoints(listeners, commands)
  }
}
