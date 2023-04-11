package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.gateway

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyMessage
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.speech.FairySpeechGateway
import com.github.unchama.targetedeffect.commandsender.MessageEffectF
import com.github.unchama.targetedeffect.player.FocusedSoundEffectF
import org.bukkit.ChatColor.{AQUA, BOLD, RESET}
import org.bukkit.Sound
import org.bukkit.entity.Player

class BukkitFairySpeechGateway[F[_]: Sync](player: Player) extends FairySpeechGateway[F] {

  override def sendMessage(fairyMessages: Seq[FairyMessage]): F[Unit] = {
    val defaultFairyMessage = s"$AQUA$BOLD<マナ妖精>$RESET%s"
    MessageEffectF[F](
      fairyMessages.map(input => defaultFairyMessage.format(input.message)).toList
    ).run(player)
  }

  override def playSpeechSound: F[Unit] =
    FocusedSoundEffectF(Sound.BLOCK_NOTE_PLING, 2.0f, 1.0f).run(player)

}
