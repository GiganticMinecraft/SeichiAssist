package com.github.unchama.seichiassist.mebius.bukkit.routines

import cats.data.NonEmptyList
import cats.effect.{IO, SyncIO}
import com.github.unchama.concurrent.{BukkitSyncIOShift, RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.playerdatarepository.JoinToQuitPlayerDataRepository
import com.github.unchama.seichiassist.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import com.github.unchama.seichiassist.mebius.domain.resources.{MebiusMessages, MebiusTalks}
import com.github.unchama.seichiassist.mebius.domain.speech.{MebiusSpeech, MebiusSpeechStrength}
import com.github.unchama.seichiassist.mebius.service.MebiusSpeechService
import com.github.unchama.util.collection.RandomizedCollection
import org.bukkit.entity.Player

import scala.concurrent.duration.FiniteDuration

object PeriodicMebiusSpeechRoutine {

  val getRepeatInterval: IO[FiniteDuration] = IO {
    import scala.concurrent.duration._

    1.minute
  }

  def unblockAndSpeakTipsOrMessageRandomly(player: Player)
                                          (implicit serviceRepository: JoinToQuitPlayerDataRepository[MebiusSpeechService[SyncIO]]): SyncIO[Unit] = {
    val service = serviceRepository(player)

    for {
      helmet <- SyncIO {
        player.getInventory.getHelmet
      }
      _ <- service.unblockSpeech()
      _ <- BukkitMebiusItemStackCodec
        .decodePropertyOfOwnedMebius(player)(helmet)
        .map { property =>
          val messageCandidates = new RandomizedCollection[String, SyncIO](
            NonEmptyList(
              MebiusTalks.at(property.level).mebiusMessage,
              MebiusMessages.tips
            )
          )

          messageCandidates.pickOne.flatMap { message =>
            service.tryMakingSpeech(property, MebiusSpeech(message, MebiusSpeechStrength.Medium))
          }
        }
        .getOrElse(SyncIO.unit)
    } yield ()
  }

  def start(player: Player)(implicit serviceRepository: JoinToQuitPlayerDataRepository[MebiusSpeechService[SyncIO]],
                            context: RepeatingTaskContext,
                            bukkitSyncIOShift: BukkitSyncIOShift): IO[Nothing] = {
    import cats.implicits._

    RepeatingRoutine.permanentRoutine(
      getRepeatInterval,
      // このタスクは同期的に実行しないとunblock -> speak -> blockの処理が入れ子になり二回走る可能性がある
      bukkitSyncIOShift.shift >> unblockAndSpeakTipsOrMessageRandomly(player).toIO
    )
  }

}
