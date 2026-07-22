package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.process

import cats.effect.Async
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.FastDiggingEffectWriteApi
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.Configuration
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.{
  FastDiggingAmplifier,
  FastDiggingEffect,
  FastDiggingEffectCause
}

import scala.concurrent.duration.DurationInt
import cats.effect.Temporal

object BreakCountEffectSynchronization {

  import cats.implicits._

  def using[F[_]: Async: Temporal, G[_], Player: HasUuid](
    implicit configuration: Configuration,
    api: FastDiggingEffectWriteApi[F, Player],
    breakCountReadAPI: BreakCountReadAPI[F, G, Player]
  ): fs2.Stream[F, Unit] = {

    breakCountReadAPI
      .batchedIncreases(1.minute)
      .evalTap(batch =>
        batch.toUuidCollatedList.traverse {
          case (player, amount) =>
            api
              .addEffect(
                FastDiggingEffect(
                  FastDiggingAmplifier(
                    (amount.amount * configuration.amplifierPerBlockMined).toDouble
                  ),
                  FastDiggingEffectCause.FromMinuteBreakCount(amount)
                ),
                1.minute
              )
              .run(player)
        }
      )
      .as(())
  }

}
