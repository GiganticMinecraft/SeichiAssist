package com.github.unchama.seichiassist.mebius

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.seichiassist.SubsystemEntryPoints
import com.github.unchama.seichiassist.domain.unsafe.SeichiAssistEffectEnvironment
import com.github.unchama.seichiassist.mebius.controller.PropertyModificationBukkitMessages
import com.github.unchama.seichiassist.mebius.controller.command.MebiusCommandExecutorProvider
import com.github.unchama.seichiassist.mebius.controller.listeners._
import com.github.unchama.seichiassist.mebius.controller.repository.SpeechGatewayRepository
import com.github.unchama.seichiassist.mebius.domain.{MebiusSpeechPresentation, PropertyModificationMessages}
import com.github.unchama.seichiassist.mebius.presentation.BukkitMebiusSpeechPresentation
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player

object EntryPoints {
  def wired(implicit effectEnvironment: SeichiAssistEffectEnvironment): SubsystemEntryPoints = {
    implicit val messages: PropertyModificationMessages = PropertyModificationBukkitMessages
    implicit val speechPresentation: MebiusSpeechPresentation[TargetedEffect[Player]] =
      new BukkitMebiusSpeechPresentation

    implicit val gatewayRepository: SpeechGatewayRepository[Kleisli[IO, Player, *]] =
      new SpeechGatewayRepository[Kleisli[IO, Player, *]]()

    val listeners = Seq(
      new MebiusDropTrialListener,
      new MebiusInteractionResponder,
      new MebiusLevelUpTrialListener,
      new MebiusPlayerJoinGreeter[IO],
      new MebiusRenamePreventionListener,
      gatewayRepository
    )

    val commands = Map(
      "mebius" -> new MebiusCommandExecutorProvider().executor
    )

    SubsystemEntryPoints(listeners, commands)
  }
}
