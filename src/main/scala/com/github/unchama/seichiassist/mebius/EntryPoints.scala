package com.github.unchama.seichiassist.mebius

import cats.effect.{IO, Timer}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.playerdatarepository.JoinToQuitPlayerDataRepository
import com.github.unchama.seichiassist.SubsystemEntryPoints
import com.github.unchama.seichiassist.domain.unsafe.SeichiAssistEffectEnvironment
import com.github.unchama.seichiassist.mebius.controller.PropertyModificationBukkitMessages
import com.github.unchama.seichiassist.mebius.controller.command.MebiusCommandExecutorProvider
import com.github.unchama.seichiassist.mebius.controller.listeners._
import com.github.unchama.seichiassist.mebius.controller.repository.{PeriodicMebiusSpeechRoutineFiberRepository, SpeechGatewayRepository}
import com.github.unchama.seichiassist.mebius.domain.{MebiusSpeechGateway, PropertyModificationMessages}
import com.github.unchama.seichiassist.mebius.gateway.BukkitMebiusSpeechGateway
import org.bukkit.entity.Player

object EntryPoints {
  def wired(implicit effectEnvironment: SeichiAssistEffectEnvironment,
            timer: Timer[IO],
            repeatingTaskContext: RepeatingTaskContext): SubsystemEntryPoints = {
    implicit val messages: PropertyModificationMessages = PropertyModificationBukkitMessages
    implicit val gatewayProvider: Player => MebiusSpeechGateway[IO] = new BukkitMebiusSpeechGateway(_)
    implicit val gatewayRepository: JoinToQuitPlayerDataRepository[MebiusSpeechGateway[IO]] = new SpeechGatewayRepository[IO]

    val listeners = Seq(
      new MebiusDropTrialListener,
      new MebiusInteractionResponder,
      new MebiusLevelUpTrialListener,
      new MebiusPlayerJoinGreeter[IO],
      new MebiusRenamePreventionListener,
      gatewayRepository,
      new PeriodicMebiusSpeechRoutineFiberRepository()
    )

    val commands = Map(
      "mebius" -> new MebiusCommandExecutorProvider().executor
    )

    SubsystemEntryPoints(listeners, commands)
  }
}
