package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.bukkit

import cats.effect.{Sync, Timer}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyMessage
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.domain.FairySpeechGateway
import com.github.unchama.targetedeffect.commandsender.MessageEffectF
import com.github.unchama.targetedeffect.player.FocusedSoundEffectF
import org.bukkit.ChatColor.{AQUA, BOLD, RESET}
import org.bukkit.Sound
import org.bukkit.entity.Player

import scala.concurrent.duration.DurationInt

class BukkitFairySpeechGateway[F[_]: Sync: Timer](player: Player)
    extends FairySpeechGateway[F] {

  override def sendMessage(fairyMessages: Seq[FairyMessage]): F[Unit] = {
    val defaultFairyMessage = s"$AQUA$BOLD<マナ妖精>$RESET%s"
    MessageEffectF[F](
      fairyMessages.map(input => defaultFairyMessage.format(input.message)).toList
    ).run(player)
  }

  import cats.implicits._

  override def playSpeechSound: F[Unit] = for {
    _ <- FocusedSoundEffectF(Sound.BLOCK_NOTE_BLOCK_PLING, 2.0f, 1.0f).run(player)
    _ <- Timer[F].sleep(100.millis)
    _ <- FocusedSoundEffectF(Sound.BLOCK_NOTE_BLOCK_PLING, 2.0f, 1.5f).run(player)
    _ <- Timer[F].sleep(100.millis)
    _ <- FocusedSoundEffectF(Sound.BLOCK_NOTE_BLOCK_PLING, 2.0f, 2.0f).run(player)
  } yield {}

}
