package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  FairyManaRecoveryState,
  FairyMessage,
  FairyMessages,
  NameCalledByFairy
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.resources.FairyMessageTable
import org.bukkit.entity.Player

import java.time.LocalTime
import scala.util.Random

class FairySpeech[F[_]: ConcurrentEffect, G[_]: ContextCoercion[*[_], F]](
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
    } yield {
      val serviceRepository = fairyAPI.fairySpeechServiceRepository(player)
      ContextCoercion {
        serviceRepository.makeSpeech(
          message,
          fairyAPI.fairyPlaySound(player.getUniqueId).toIO.unsafeRunSync()
        )
      }.toIO.unsafeRunSync()
    }

  def speechRandomly(
    player: Player,
    fairyManaRecoveryState: FairyManaRecoveryState
  ): F[Unit] = {
    val nameCalledByFairy = NameCalledByFairy(player.getName)
    val messages = fairyManaRecoveryState match {
      case FairyManaRecoveryState.full =>
        FairyMessageTable.manaFullMessages
      case FairyManaRecoveryState.consumptionApple =>
        FairyMessageTable.consumed
      case FairyManaRecoveryState.notConsumptionApple =>
        FairyMessageTable.notConsumed
    }
    randomMessage(messages(nameCalledByFairy)).map { message =>
      ContextCoercion {
        fairyAPI
          .fairySpeechServiceRepository(player)
          .makeSpeech(message, fairyAPI.fairyPlaySound(player.getUniqueId).toIO.unsafeRunSync())
      }.toIO.unsafeRunSync()
    }
  }

  def speechEndTime(player: Player): F[Unit] = for {
    endTimeOpt <- fairyAPI.fairyEndTime(player)
    playSound <- fairyAPI.fairyPlaySound(player.getUniqueId)
  } yield {
    val endTime = endTimeOpt.get.endTimeOpt.get
    ContextCoercion {
      fairyAPI
        .fairySpeechServiceRepository(player)
        .makeSpeech(
          FairyMessage(s"僕は${endTime.getHour}:${endTime.getMinute}には帰るよー。"),
          playSound
        )
    }.toIO.unsafeRunSync()
  }

  def welcomeBack(player: Player): F[Unit] = for {
    playSound <- fairyAPI.fairyPlaySound(player.getUniqueId)
  } yield {
    ContextCoercion {
      fairyAPI
        .fairySpeechServiceRepository(player)
        .makeSpeech(FairyMessage(s"おかえり！${player.getName}"), playSound)
    }.toIO.unsafeRunSync()
  }

  def bye(player: Player): F[Unit] = for {
    playSound <- fairyAPI.fairyPlaySound(player.getUniqueId)
  } yield {
    ContextCoercion {
      fairyAPI
        .fairySpeechServiceRepository(player)
        .makeSpeech(
          FairyMessage(s"""あっ、もうこんな時間だ！
                          |じゃーねー！${player.getName}
                          |""".stripMargin),
          playSound
        )
    }.toIO.unsafeRunSync()
  }

  private def randomMessage(fairyMessages: FairyMessages): F[FairyMessage] = Sync[F].delay {
    val messages = fairyMessages.messages
    messages(Random.nextInt(messages.size))
  }

}
