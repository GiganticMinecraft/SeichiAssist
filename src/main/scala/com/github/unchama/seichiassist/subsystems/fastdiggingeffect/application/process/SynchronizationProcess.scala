package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.process

import cats.{Monad, MonadError}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.actions.GrantFastDiggingEffect
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.FastDiggingEffectList
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.FastDiggingEffectSuppressionState
import io.chrisdavenport.cats.effect.time.JavaTime

object SynchronizationProcess {

  import cats.implicits._

  def using[F[_]: GrantFastDiggingEffect[*[_], Player]: MonadError[
    *[_],
    Throwable
  ]: JavaTime, Player](
    suppressionState: KeyedDataRepository[
      Player,
      ReadOnlyRef[F, FastDiggingEffectSuppressionState]
    ],
    effectClock: fs2.Stream[F, (Player, FastDiggingEffectList)]
  ): fs2.Stream[F, Unit] =
    effectClock
      .evalTap {
        case (player, list) =>
          for {
            state <- suppressionState
              .lift(player)
              .map(_.read)
              .getOrElse(Monad[F].pure(FastDiggingEffectSuppressionState.Disabled))

            totalAmplifier <- list.totalPotionAmplifier[F](state)

            _ <- totalAmplifier.traverse {
              // クロックが完全に同期していない(厳密に20ティックに1回ではない)ため、2秒間の効果を付与する
              GrantFastDiggingEffect[F, Player].forTwoSeconds(player)
            }
          } yield ()
      }
      .as(())

}
