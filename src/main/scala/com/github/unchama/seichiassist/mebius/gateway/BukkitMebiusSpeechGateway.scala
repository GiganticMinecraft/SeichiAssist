package com.github.unchama.seichiassist.mebius.gateway

import java.util.concurrent.TimeUnit

import cats.effect.{IO, Timer}
import com.github.unchama.seichiassist.mebius.domain.{MebiusProperty, MebiusSpeechGateway, MebiusSpeechStrength}
import com.github.unchama.targetedeffect.{DelayEffect, SequentialEffect, TargetedEffect}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.{ChatColor, Sound}
import org.bukkit.entity.Player

import scala.concurrent.duration.FiniteDuration

class BukkitMebiusSpeechGateway(player: Player)(implicit timer: Timer[IO]) extends MebiusSpeechGateway[IO] {

  override protected def sendMessage(property: MebiusProperty, message: String): IO[Unit] = {
    MessageEffect(
      s"${ChatColor.RESET}<${property.mebiusName}${ChatColor.RESET}> $message"
    ).run(player)
  }

  override protected def playSpeechSound(strength: MebiusSpeechStrength): IO[Unit] = {
    def playSoundsInSequence(firstSound: TargetedEffect[Player], secondSound: TargetedEffect[Player]): TargetedEffect[Player] =
      SequentialEffect(
        firstSound,
        DelayEffect(FiniteDuration(100, TimeUnit.MILLISECONDS)),
        secondSound
      )

    val effect = strength match {
      case MebiusSpeechStrength.Medium =>
        playSoundsInSequence(
          FocusedSoundEffect(Sound.BLOCK_NOTE_HARP, 2.0f, 1.0f),
          FocusedSoundEffect(Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f)
        )
      case MebiusSpeechStrength.Loud =>
        playSoundsInSequence(
          FocusedSoundEffect(Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f),
          FocusedSoundEffect(Sound.BLOCK_NOTE_HARP, 2.0f, 2.0f),
        )
    }

    effect.run(player)
  }
}
