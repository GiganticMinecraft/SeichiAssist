package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions

import cats.Monad
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
import org.bukkit.ChatColor._

import java.time.LocalTime
import scala.util.Random

object BukkitFairySpeak {

  import cats.implicits._

  def apply[F[_]: ConcurrentEffect]: FairySpeak[F, Player] = new FairySpeak[F, Player] {
    override def speak(player: Player, fairyMessage: FairyMessage)(
      implicit fairyAPI: FairyAPI[F, Player]
    ): F[Unit] = for {
      playSound <- fairyAPI.fairyPlaySound(player.getUniqueId)
    } yield {
      val message = s"$AQUA$BOLD<マナ妖精>$RESET${fairyMessage.message}"
      if (playSound == FairyPlaySound.on)
        SequentialEffect(
          FocusedSoundEffect(Sound.BLOCK_NOTE_PLING, 2.0f, 1.0f),
          MessageEffect(message)
        ).run(player).unsafeRunSync()
      else MessageEffect(message).run(player).unsafeRunSync()
    }

    override def speakRandomly(
      player: Player
    )(implicit fairyAPI: FairyAPI[F, Player]): F[Unit] = {
      Sync[F].unit
    }

    override def speakStartMessage(
      player: Player
    )(implicit fairyAPI: FairyAPI[F, Player]): F[Unit] = {
      val nameCalledByFairy = NameCalledByFairy(player.getName)
      val uuid = player.getUniqueId

      implicit val F: Monad[F] = implicitly

      F.ifM(fairyAPI.fairyUsingState(uuid).map(_ == FairyUsingState.NotUsing))(
        return Sync[F].unit,
        Sync[F].unit
      )
      val startHour = LocalTime.now().getHour

      val fairyMessages =
        if (4 <= startHour && startHour < 10)
          FairyMessageTable.morningMessages(nameCalledByFairy)
        else if (10 <= startHour && startHour < 18)
          FairyMessageTable.dayMessages(nameCalledByFairy)
        else
          FairyMessageTable.nightMessages(nameCalledByFairy)

      getMessageRandomly(fairyMessages).map(speak(player, _).toIO.unsafeRunAsyncAndForget())
    }
  }

  private def getMessageRandomly[F[_]: Sync](fairyMessages: FairyMessages): F[FairyMessage] =
    Sync[F].delay {
      val messages = fairyMessages.messages
      messages(Random.nextInt(messages.size))
    }

}
