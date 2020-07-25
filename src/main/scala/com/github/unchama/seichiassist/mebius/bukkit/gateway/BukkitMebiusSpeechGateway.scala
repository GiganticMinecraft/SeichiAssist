package com.github.unchama.seichiassist.mebius.bukkit.gateway

import java.util.concurrent.TimeUnit

import cats.effect.{IO, Timer}
import com.github.unchama.seichiassist.mebius.domain.property.MebiusProperty
import com.github.unchama.seichiassist.mebius.domain.speech.{MebiusSpeechGateway, MebiusSpeechStrength}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{DelayEffect, RepeatedEffect, SequentialEffect, TargetedEffect}
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player

import scala.concurrent.duration.FiniteDuration

class BukkitMebiusSpeechGateway(player: Player)(implicit timer: Timer[IO]) extends MebiusSpeechGateway[IO] {

  override def sendMessage(property: MebiusProperty, message: String): IO[Unit] = {
    MessageEffect(
      s"$RESET<${property.mebiusName}$RESET> $message"
    ).run(player)
  }

  override def playSpeechSound(strength: MebiusSpeechStrength): IO[Unit] = {
    def playSoundsInSequence(firstSound: TargetedEffect[Player], secondSound: TargetedEffect[Player]): TargetedEffect[Player] =
      SequentialEffect(
        firstSound,
        DelayEffect(FiniteDuration(100, TimeUnit.MILLISECONDS)),
        secondSound
      )

    val effect = strength match {
      case MebiusSpeechStrength.Medium =>
        playSoundsInSequence(
          RepeatedEffect(3)(FocusedSoundEffect(Sound.BLOCK_NOTE_HARP, 2.0f, 1.0f)),
          RepeatedEffect(3)(FocusedSoundEffect(Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f))
        )
      case MebiusSpeechStrength.Loud =>
        playSoundsInSequence(
          RepeatedEffect(5)(FocusedSoundEffect(Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f)),
          RepeatedEffect(5)(FocusedSoundEffect(Sound.BLOCK_NOTE_HARP, 2.0f, 2.0f)),
        )
    }

    effect.run(player)
  }
}
