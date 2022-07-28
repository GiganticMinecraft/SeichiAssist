package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, Sync}
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

class FairySpeech[F[_]: ConcurrentEffect](implicit fairyAPI: FairyAPI[F, Player]) {

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
      serviceRepository
        .makeSpeech(message, fairyAPI.fairyPlaySound(player.getUniqueId).toIO.unsafeRunSync())
        .unsafeRunSync()
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
      case FairyMessageTable.notConsumed =>
        FairyMessageTable.notConsumed
    }
    randomMessage(messages(nameCalledByFairy)).map { message =>
      fairyAPI
        .fairySpeechServiceRepository(player)
        .makeSpeech(message, fairyAPI.fairyPlaySound(player.getUniqueId).toIO.unsafeRunSync())
        .unsafeRunSync()
    }
  }

  private def randomMessage(fairyMessages: FairyMessages): F[FairyMessage] = Sync[F].delay {
    val messages = fairyMessages.messages
    messages(Random.nextInt(messages.size))
  }

}
