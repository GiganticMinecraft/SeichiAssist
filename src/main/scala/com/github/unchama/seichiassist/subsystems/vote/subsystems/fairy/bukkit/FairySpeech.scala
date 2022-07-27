package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, Sync, SyncIO}
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  FairyMessage,
  FairyMessages,
  NameCalledByFairy
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.resources.FairyMessageTable
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.service.FairySpeechService
import org.bukkit.entity.Player

import java.time.LocalTime
import scala.util.Random

class FairySpeech[F[_]: ConcurrentEffect](
  implicit serviceRepository: PlayerDataRepository[FairySpeechService[SyncIO]],
  fairyAPI: FairyAPI[F, Player]
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
      serviceRepository(player)
        .makeSpeech(message, fairyAPI.fairyPlaySound(player.getUniqueId).toIO.unsafeRunSync())
    }

  // TODO: 妖精によるマナ回復関連が定義されたこれを定義する
//  def speechRandomly(player: Player): F[Unit] = {
//    val nameCalledByFairy = NameCalledByFairy(player.getName)
//
//  }

  private def randomMessage(fairyMessages: FairyMessages): F[FairyMessage] = Sync[F].delay {
    val messages = fairyMessages.messages
    messages(Random.nextInt(messages.size))
  }

}
