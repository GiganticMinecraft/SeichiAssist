package com.github.unchama.seichiassist.mebius.bukkit.routines

import cats.data.NonEmptyList
import cats.effect.IO
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.playerdatarepository.JoinToQuitPlayerDataRepository
import com.github.unchama.seichiassist.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import com.github.unchama.seichiassist.mebius.domain.resources.{MebiusMessages, MebiusTalks}
import com.github.unchama.seichiassist.mebius.domain.speech.{MebiusSpeech, MebiusSpeechStrength}
import com.github.unchama.seichiassist.mebius.service.MebiusSpeechService
import com.github.unchama.util.collection.RandomizedCollection
import org.bukkit.entity.Player

import scala.concurrent.duration.FiniteDuration

object PeriodicMebiusSpeechRoutine {

  def start(player: Player)(implicit serviceRepository: JoinToQuitPlayerDataRepository[MebiusSpeechService[IO]],
                            context: RepeatingTaskContext): IO[Nothing] = {
    val getRepeatInterval: IO[FiniteDuration] = IO {
      import scala.concurrent.duration._

      1.minute
    }

    val service = serviceRepository(player)

    val speakTipsOrMessageRandomly: IO[Unit] = for {
      helmet <- IO {
        player.getInventory.getHelmet
      }
      _ <- service.unblockSpeech()
      _ <- BukkitMebiusItemStackCodec
        .decodePropertyOfOwnedMebius(player)(helmet)
        .map { property =>
          val messageCandidates = new RandomizedCollection[String, IO](
            NonEmptyList(
              MebiusTalks.at(property.level).mebiusMessage,
              MebiusMessages.tips
            )
          )

          messageCandidates.pickOne.flatMap { message =>
            service.tryMakingSpeech(property, MebiusSpeech(message, MebiusSpeechStrength.Medium))
          }
        }
        .getOrElse(IO.unit)
    } yield ()

    RepeatingRoutine.permanentRoutine(getRepeatInterval, speakTipsOrMessageRandomly)
  }

}
