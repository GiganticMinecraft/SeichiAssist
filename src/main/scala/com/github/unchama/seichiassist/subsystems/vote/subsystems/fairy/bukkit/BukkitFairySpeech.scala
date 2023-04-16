package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairyPersistence
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.resources.FairyMessageTable
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.speech.FairySpeech
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.FairySpeechAPI
import io.chrisdavenport.cats.effect.time.JavaTime
import org.bukkit.entity.Player

import java.time.ZoneId
import scala.util.Random

class BukkitFairySpeech[F[_]: Sync: JavaTime](
  implicit fairyPersistence: FairyPersistence[F],
  fairySpeechAPI: FairySpeechAPI[F, Player]
) extends FairySpeech[F, Player] {

  import cats.implicits._

  private def getSummonMessagesByStartHour(
    startHour: Int,
    nameCalledByFairy: ScreenNameForFairy
  ): FairyMessageChoice = {
    if (4 <= startHour && startHour < 10)
      FairyMessageTable.morningMessages(nameCalledByFairy)
    else if (10 <= startHour && startHour < 18)
      FairyMessageTable.dayMessages(nameCalledByFairy)
    else
      FairyMessageTable.nightMessages(nameCalledByFairy)
  }

  override def summonSpeech(player: Player): F[Unit] =
    for {
      startHour <- JavaTime[F].getLocalDateTime(ZoneId.systemDefault()).map(_.getHour)
      nameCalledByFairy = ScreenNameForFairy(player.getName)
      fairyMessages = getSummonMessagesByStartHour(startHour, nameCalledByFairy)
      message <- randomMessage(fairyMessages)
      _ <- fairySpeechAPI.speech(player, Seq(message))
    } yield ()

  override def speechRandomly(
    player: Player,
    fairyManaRecoveryState: FairyManaRecoveryState
  ): F[Unit] = {
    val nameCalledByFairy = ScreenNameForFairy(player.getName)
    val messages = fairyManaRecoveryState match {
      case FairyManaRecoveryState.Full =>
        FairyMessageTable.manaFullMessages
      case FairyManaRecoveryState.RecoveredWithApple =>
        FairyMessageTable.consumed
      case FairyManaRecoveryState.RecoveredWithoutApple =>
        FairyMessageTable.notConsumed
    }
    for {
      message <- randomMessage(messages(nameCalledByFairy))
      _ <- fairySpeechAPI.speech(player, Seq(message))
    } yield ()
  }

  override def speechEndTime(player: Player): F[Unit] = {
    for {
      endTimeOpt <- fairyPersistence.fairyEndTime(player.getUniqueId)
      endTime = endTimeOpt.get.endTime
      _ <- fairySpeechAPI.speech(
        player,
        Seq(FairyMessage(s"僕は${endTime.getHour}:${endTime.getMinute}には帰るよー。"))
      )
    } yield ()
  }

  override def welcomeBack(player: Player): F[Unit] = for {
    _ <- fairySpeechAPI.speech(player, Seq(FairyMessage(s"おかえり！${player.getName}")))
  } yield ()

  override def bye(player: Player): F[Unit] = for {
    _ <- fairySpeechAPI.speech(
      player,
      Seq(FairyMessage(s"あっ、もうこんな時間だ！"), FairyMessage(s"じゃーねー！${player.getName}"))
    )
  } yield ()

  private def randomMessage(fairyMessages: FairyMessageChoice): F[FairyMessage] =
    Sync[F].delay {
      val messages = fairyMessages.messages.toVector
      messages(Random.nextInt(messages.size))
    }

}
