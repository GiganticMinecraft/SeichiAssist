package com.github.unchama.seichiassist.mebius.bukkit.routines

import cats.effect.IO
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.playerdatarepository.JoinToQuitPlayerDataRepository
import com.github.unchama.seichiassist.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import com.github.unchama.seichiassist.mebius.domain.resources.{MebiusMessages, MebiusTalks}
import com.github.unchama.seichiassist.mebius.domain.speech.{MebiusSpeech, MebiusSpeechGateway, MebiusSpeechStrength}
import org.bukkit.entity.Player

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object PeriodicMebiusSpeechRoutine {

  def start(player: Player)(implicit gatewayRepository: JoinToQuitPlayerDataRepository[MebiusSpeechGateway[IO]],
                            context: RepeatingTaskContext): IO[Nothing] = {
    val getRepeatInterval: IO[FiniteDuration] = IO {
      import scala.concurrent.duration._

      1.minute
    }

    val gateway = gatewayRepository(player)

    val speakTipsOrMessageRandomly: IO[Unit] = for {
      helmet <- IO {
        player.getInventory.getHelmet
      }
      _ <- gateway.unblockSpeech()
      _ <- BukkitMebiusItemStackCodec.decodeMebiusProperty(helmet)
        .map { property =>
          val messagesCandidate = MebiusMessages.tips.appended(MebiusTalks.at(property.level).mebiusMessage)
          val message = messagesCandidate(Random.nextInt(messagesCandidate.size))

          gateway.tryMakingSpeech(property, MebiusSpeech(message, MebiusSpeechStrength.Medium))
        }
        .getOrElse(IO.unit)
    } yield ()

    RepeatingRoutine.permanentRoutine(getRepeatInterval, speakTipsOrMessageRandomly)
  }

}
