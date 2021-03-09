package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.process

import cats.effect.{ConcurrentEffect, Timer}
import com.github.unchama.minecraft.actions.GetConnectedPlayers
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.FastDiggingEffectApi
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.Configuration
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.{FastDiggingAmplifier, FastDiggingEffect, FastDiggingEffectCause}

object PlayerCountEffectSynchronization {

  import cats.implicits._

  import scala.concurrent.duration._

  def using[
    F[_] : ConcurrentEffect : Timer : GetConnectedPlayers[*[_], Player],
    Player: HasUuid
  ](implicit
    configuration: Configuration,
    api: FastDiggingEffectApi[F, Player]): fs2.Stream[F, Unit] = {

    fs2.Stream
      .awakeEvery[F](1.minute)
      .evalMap { _ =>
        GetConnectedPlayers[F, Player].now
          .flatMap { players =>
            val count = players.size

            players.traverse { player =>
              api.addEffect(
                FastDiggingEffect(
                  FastDiggingAmplifier(configuration.amplifierPerPlayerConnection * count),
                  FastDiggingEffectCause.FromConnectionNumber(count)
                ),
                1.minute
              ).run(player)
            }
          }
          .as(())
      }
  }

}
