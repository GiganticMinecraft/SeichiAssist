package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.FairySpeak
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain._
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.Sound
import org.bukkit.entity.Player

import scala.util.Random

object BukkitFairySpeak {

  import cats.implicits._

  def apply[F[_]: ConcurrentEffect]: FairySpeak[F, Player] = new FairySpeak[F, Player] {
    override def speak(player: Player, fairyMessage: FairyMessage)(
      implicit fairyAPI: FairyAPI[F]
    ): F[Unit] = for {
      playSound <- fairyAPI.fairyPlaySound(player.getUniqueId)
    } yield {
      if (playSound == FairyPlaySound.play)
        SequentialEffect(
          FocusedSoundEffect(Sound.BLOCK_NOTE_PLING, 2.0f, 1.0f),
          MessageEffect(fairyMessage.message)
        ).apply(player)
      else SequentialEffect(MessageEffect(fairyMessage.message)).apply(player)
    }

    override def speakRandomly(player: Player)(implicit fairyAPI: FairyAPI[F]): F[Unit] = {
      val nameCalledByFairy = NameCalledByFairy(player.getName)
      val uuid = player.getUniqueId

      if (fairyAPI.fairyUsingState(uuid).toIO.unsafeRunSync() == FairyUsingState.NotUsing)
        return Sync[F].unit

      for {
        fairyValidTimesOpt <- fairyAPI.fairyValidTimes(uuid)
        startTimeHour = fairyValidTimesOpt.getOrElse(return Sync[F].unit).startTime.getHour
        fairyMessages =
          if (4 <= startTimeHour && startTimeHour < 10)
            FairyMessageTable.morningMessages(nameCalledByFairy)
          else if (10 <= startTimeHour && startTimeHour < 18)
            FairyMessageTable.dayMessages(nameCalledByFairy)
          else
            FairyMessageTable.nightMessages(nameCalledByFairy)
        fairyMessage <- getMessageRandomly(fairyMessages)
      } yield speak(player, fairyMessage)
    }
  }

  private def getMessageRandomly[F[_]: Sync](fairyMessages: FairyMessages): F[FairyMessage] =
    Sync[F].delay {
      val messages = fairyMessages.messages
      messages(Random.nextInt(messages.size))
    }

}
