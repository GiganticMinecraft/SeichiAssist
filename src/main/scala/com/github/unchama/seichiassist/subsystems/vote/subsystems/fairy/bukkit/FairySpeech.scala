package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit

import cats.effect.Sync
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.resources.FairyMessageTable
import org.bukkit.entity.Player

import java.time.LocalTime
import scala.util.Random

class FairySpeech[F[_]: Sync, G[_]: ContextCoercion[*[_], F]](
  implicit fairyAPI: FairyAPI[F, G, Player]
) {

  import cats.implicits._

  def summonSpeech(player: Player): F[Unit] =
    for {
      startHour <- Sync[F].delay(LocalTime.now().getHour)
      nameCalledByFairy = NameCalledByFairy(player.getName)
      fairyMessages =
        if (4 <= startHour && startHour < 10)
          FairyMessageTable.morningMessages(nameCalledByFairy)
        else if (10 <= startHour && startHour < 18)
          FairyMessageTable.dayMessages(nameCalledByFairy)
        else
          FairyMessageTable.nightMessages(nameCalledByFairy)
      message <- randomMessage(fairyMessages)

      serviceRepository = fairyAPI.fairySpeechServiceRepository(player)
      fairySpeechSound <- ContextCoercion {
        fairyAPI.fairySpeechSound(player.getUniqueId)
      }
      _ <- ContextCoercion {
        serviceRepository.makeSpeech(message, fairySpeechSound)
      }
    } yield ()

  def speechRandomly(
    player: Player,
    fairyManaRecoveryState: FairyManaRecoveryState
  ): F[Unit] = {
    val nameCalledByFairy = NameCalledByFairy(player.getName)
    val messages = fairyManaRecoveryState match {
      case FairyManaRecoveryState.Full =>
        FairyMessageTable.manaFullMessages
      case FairyManaRecoveryState.ConsumptionApple =>
        FairyMessageTable.consumed
      case FairyManaRecoveryState.NotConsumptionApple =>
        FairyMessageTable.notConsumed
    }
    for {
      message <- randomMessage(messages(nameCalledByFairy))
      fairyPlaySound <- fairyAPI.fairySpeechSound(player.getUniqueId)
      _ <- ContextCoercion {
        fairyAPI.fairySpeechServiceRepository(player).makeSpeech(message, fairyPlaySound)
      }
    } yield ()
  }

  def speechEndTime(player: Player): F[Unit] = {
    for {
      endTimeOpt <- fairyAPI.fairyEndTime(player)
      playSound <- fairyAPI.fairySpeechSound(player.getUniqueId)
      endTime = endTimeOpt.get.endTimeOpt.get
      _ <- ContextCoercion {
        fairyAPI
          .fairySpeechServiceRepository(player)
          .makeSpeech(
            FairyMessage(s"僕は${endTime.getHour}:${endTime.getMinute}には帰るよー。"),
            playSound
          )
      }
    } yield ()
  }

  def welcomeBack(player: Player): F[Unit] = for {
    playSound <- fairyAPI.fairySpeechSound(player.getUniqueId)
    _ <- ContextCoercion {
      fairyAPI
        .fairySpeechServiceRepository(player)
        .makeSpeech(FairyMessage(s"おかえり！${player.getName}"), playSound)
    }
  } yield ()

  def bye(player: Player): F[Unit] = for {
    playSound <- fairyAPI.fairySpeechSound(player.getUniqueId)
    repository = fairyAPI.fairySpeechServiceRepository(player)
    _ <- ContextCoercion {
      repository.makeSpeech(FairyMessage(s"あっ、もうこんな時間だ！"), FairyPlaySound.Off)
    } >> ContextCoercion {
      repository.makeSpeech(FairyMessage(s"じゃーねー！${player.getName}"), playSound)
    }
  } yield ()

  private def randomMessage(fairyMessages: FairyMessages): F[FairyMessage] = Sync[F].delay {
    val messages = fairyMessages.messages.toVector
    messages(Random.nextInt(messages.size))
  }

}
