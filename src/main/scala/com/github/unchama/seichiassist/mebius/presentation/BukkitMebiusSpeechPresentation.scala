package com.github.unchama.seichiassist.mebius.presentation

import cats.kernel.Monoid
import com.github.unchama.seichiassist.mebius.domain.{MebiusProperty, MebiusSpeechPresentation, MebiusSpeechStrength}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.entity.Player
import org.bukkit.{ChatColor, Sound}

class BukkitMebiusSpeechPresentation extends MebiusSpeechPresentation[TargetedEffect[Player]] {
  type Effect = TargetedEffect[Player]

  override val Effect: Monoid[Effect] = {
    import cats.implicits._

    // `TargetedEffect[Player]` は `Kleisli[IO, Player, Unit]` なので効果が結合できて `Monoid` にできる
    implicitly
  }

  override def sendMessage(property: MebiusProperty, message: String): Effect = {
    MessageEffect(s"${ChatColor.RESET}<${property.mebiusName}${ChatColor.RESET}> $message")
  }

  override def playSpeechSound(strength: MebiusSpeechStrength): Effect = {
    def playSoundsInSequence(firstSound: Effect, secondSound: Effect): Effect = {
      ???
    }

    strength match {
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
  }
}
