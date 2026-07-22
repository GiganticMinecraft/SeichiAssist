package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.process

import cats.effect.Async
import com.github.unchama.minecraft.actions.GetConnectedPlayers
import com.github.unchama.seichiassist.domain.actions.GetNetworkConnectionCount
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.FastDiggingEffectApi
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.Configuration
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.{
  FastDiggingAmplifier,
  FastDiggingEffect,
  FastDiggingEffectCause
}
import cats.effect.Temporal

object PlayerCountEffectSynchronization {

  import cats.implicits._

  import scala.concurrent.duration._

  def using[F[_]: Async: Temporal: [f[_]] =>> GetConnectedPlayers[
    f,
    Player
  ]: GetNetworkConnectionCount, Player](
    implicit configuration: Configuration,
    api: FastDiggingEffectApi[F, Player]
  ): fs2.Stream[F, Unit] = {

    fs2.Stream.awakeEvery[F](1.minute).evalMap { _ =>
      for {
        count <- GetNetworkConnectionCount[F].now
        players <- GetConnectedPlayers[F, Player].now
        _ <- players.traverse { player =>
          api
            .addEffect(
              FastDiggingEffect(
                FastDiggingAmplifier(configuration.amplifierPerPlayerConnection * count),
                FastDiggingEffectCause.FromConnectionNumber(count)
              ),
              1.minute
            )
            .run(player)
        }
      } yield ()
    }
  }

}
