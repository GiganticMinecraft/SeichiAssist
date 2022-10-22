package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit

import cats.effect.Sync
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.resources.FairyMessageTable
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{
  FairyPersistence,
  FairySpeech
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.service.FairySpeechService
import org.bukkit.entity.Player

import java.time.LocalTime
import scala.util.Random

class BukkitFairySpeech[F[_]: Sync, G[_]: ContextCoercion[*[_], F]](
  fairySpeechServiceRepository: PlayerDataRepository[FairySpeechService[G]],
  fairyPersistence: FairyPersistence[F]
) extends FairySpeech[F, Player] {

  import cats.implicits._

  override def summonSpeech(player: Player): F[Unit] =
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

      serviceRepository = fairySpeechServiceRepository(player)
      fairySpeechSound <- ContextCoercion {
        fairyPersistence.fairySpeechSound(player.getUniqueId)
      }
      _ <- ContextCoercion {
        serviceRepository.makeSpeech(message, fairySpeechSound)
      }
    } yield ()

  override def speechRandomly(
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
      fairyPlaySound <- fairyPersistence.fairySpeechSound(player.getUniqueId)
      _ <- ContextCoercion {
        fairySpeechServiceRepository(player).makeSpeech(message, fairyPlaySound)
      }
    } yield ()
  }

  override def speechEndTime(player: Player): F[Unit] = {
    for {
      endTimeOpt <- fairyPersistence.fairyEndTime(player.getUniqueId)
      playSound <- fairyPersistence.fairySpeechSound(player.getUniqueId)
      endTime = endTimeOpt.get.endTimeOpt.get
      _ <- ContextCoercion {
        fairySpeechServiceRepository(player).makeSpeech(
          FairyMessage(s"僕は${endTime.getHour}:${endTime.getMinute}には帰るよー。"),
          playSound
        )
      }
    } yield ()
  }

  override def welcomeBack(player: Player): F[Unit] = for {
    playSound <- fairyPersistence.fairySpeechSound(player.getUniqueId)
    _ <- ContextCoercion {
      fairySpeechServiceRepository(player)
        .makeSpeech(FairyMessage(s"おかえり！${player.getName}"), playSound)
    }
  } yield ()

  override def bye(player: Player): F[Unit] = for {
    playSound <- fairyPersistence.fairySpeechSound(player.getUniqueId)
    repository = fairySpeechServiceRepository(player)
    _ <- ContextCoercion {
      repository.makeSpeech(FairyMessage(s"あっ、もうこんな時間だ！"), fairyPlaySound = false)
    } >> ContextCoercion {
      repository.makeSpeech(FairyMessage(s"じゃーねー！${player.getName}"), playSound)
    }
  } yield ()

  private def randomMessage(fairyMessages: FairyMessages): F[FairyMessage] = Sync[F].delay {
    val messages = fairyMessages.messages.toVector
    messages(Random.nextInt(messages.size))
  }

}
