package com.github.unchama.seichiassist.mebius.gateway

import cats.effect.IO
import com.github.unchama.seichiassist.mebius.domain.{MebiusProperty, MebiusSpeechGateway, MebiusSpeechStrength}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.{ChatColor, Sound}
import org.bukkit.entity.Player

class BukkitMebiusSpeechGateway(player: Player) extends MebiusSpeechGateway[IO] {

  override protected def sendMessage(property: MebiusProperty, message: String): IO[Unit] = {
    MessageEffect(
      s"${ChatColor.RESET}<${property.mebiusName}${ChatColor.RESET}> $message"
    ).run(player)
  }

  override protected def playSpeechSound(strength: MebiusSpeechStrength): IO[Unit] = {
    def playSoundsInSequence(firstSound: TargetedEffect[Player], secondSound: TargetedEffect[Player]): TargetedEffect[Player] = {
      ???
    }

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
