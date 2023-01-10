package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit

import cats.effect.Sync
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.resources.FairyMessageTable
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairyPersistence
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.speech.FairySpeech
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.service.FairySpeechService
import org.bukkit.entity.Player

import java.time.LocalTime
import scala.util.Random

class BukkitFairySpeech[F[_]: Sync, G[_]: ContextCoercion[*[_], F]](
  fairySpeechServiceRepository: PlayerDataRepository[FairySpeechService[G]],
  fairyPersistence: FairyPersistence[F]
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
      startHour <- Sync[F].delay(LocalTime.now().getHour)
      nameCalledByFairy = ScreenNameForFairy(player.getName)
      fairyMessages = getSummonMessagesByStartHour(startHour, nameCalledByFairy)
      message <- randomMessage(fairyMessages)

      serviceRepository = fairySpeechServiceRepository(player)
      fairySpeechSound <- ContextCoercion {
        fairyPersistence.playSoundOnFairySpeech(player.getUniqueId)
      }
      _ <- ContextCoercion {
        serviceRepository.makeSpeech(Seq(message), fairySpeechSound)
      }
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
      fairyPlaySound <- fairyPersistence.playSoundOnFairySpeech(player.getUniqueId)
      _ <- ContextCoercion {
        fairySpeechServiceRepository(player).makeSpeech(Seq(message), fairyPlaySound)
      }
    } yield ()
  }

  override def speechEndTime(player: Player): F[Unit] = {
    for {
      endTimeOpt <- fairyPersistence.fairyEndTime(player.getUniqueId)
      playSound <- fairyPersistence.playSoundOnFairySpeech(player.getUniqueId)
      endTime = endTimeOpt.get.endTimeOpt.get
      _ <- ContextCoercion {
        fairySpeechServiceRepository(player).makeSpeech(
          Seq(FairyMessage(s"僕は${endTime.getHour}:${endTime.getMinute}には帰るよー。")),
          playSound
        )
      }
    } yield ()
  }

  override def welcomeBack(player: Player): F[Unit] = for {
    playSound <- fairyPersistence.playSoundOnFairySpeech(player.getUniqueId)
    _ <- ContextCoercion {
      fairySpeechServiceRepository(player)
        .makeSpeech(Seq(FairyMessage(s"おかえり！${player.getName}")), playSound)
    }
  } yield ()

  override def bye(player: Player): F[Unit] = for {
    playSound <- fairyPersistence.playSoundOnFairySpeech(player.getUniqueId)
    repository = fairySpeechServiceRepository(player)
    _ <- ContextCoercion {
      repository.makeSpeech(
        Seq(FairyMessage(s"あっ、もうこんな時間だ！"), FairyMessage(s"じゃーねー！${player.getName}")),
        playSound
      )
    }
  } yield ()

  private def randomMessage(fairyMessages: FairyMessageChoice): F[FairyMessage] =
    Sync[F].delay {
      val messages = fairyMessages.messages.toVector
      messages(Random.nextInt(messages.size))
    }

}
