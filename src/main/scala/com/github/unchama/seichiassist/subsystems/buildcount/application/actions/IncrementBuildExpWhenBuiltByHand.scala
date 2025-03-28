package com.github.unchama.seichiassist.subsystems.buildcount.application.actions

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{Effect, Sync}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.fs2.workaround.fs3.Fs3Topic
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.ratelimiting.RateLimiter
import com.github.unchama.seichiassist.subsystems.buildcount.application.BuildExpMultiplier
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData

/**
 * [[Player]] が手でブロックを設置した際に建築量を加算するアクションを提供する型クラス。
 */
trait IncrementBuildExpWhenBuiltByHand[F[_], Player] {

  def of(player: Player): F[Unit] = of(player, BuildExpAmount(1))

  def of(player: Player, by: BuildExpAmount): F[Unit]

}

object IncrementBuildExpWhenBuiltByHand {

  import cats.implicits._

  def apply[F[_], Player](
    implicit ev: IncrementBuildExpWhenBuiltByHand[F, Player]
  ): IncrementBuildExpWhenBuiltByHand[F, Player] = ev

  def using[F[_]: ClassifyPlayerWorld[*[_], Player], G[_]: Effect, Player](
    rateLimiterRepository: KeyedDataRepository[Player, RateLimiter[F, BuildExpAmount]],
    dataRepository: KeyedDataRepository[Player, Ref[F, BuildAmountData]],
    dataTopic: Fs3Topic[G, (Player, BuildAmountData)]
  )(
    implicit multiplier: BuildExpMultiplier,
    sync: Sync[F]
  ): IncrementBuildExpWhenBuiltByHand[F, Player] =
    (player: Player, by: BuildExpAmount) => {
      val F: Monad[F] = implicitly

      for {
        amountToIncrement <-
          // ワールドの判定はここで行われるべき。[[IncrementBuildExpWhenBuiltBySkill]]はこの値を参照する。
          F.ifM(ClassifyPlayerWorld[F, Player].isInBuildWorld(player))(
            F.ifF(ClassifyPlayerWorld[F, Player].isInSeichiWorld(player))(
              by.mapAmount(_ * multiplier.whenInSeichiWorld),
              by
            ),
            F.pure(BuildExpAmount(0))
          )

        // レートリミッターで制限しないと当然無制限になるので注意！！！
        cappedIncreasedAmount <-
          rateLimiterRepository(player).requestPermission(amountToIncrement)
        incrementedData <-
          dataRepository(player).updateAndGet(_.addExpAmount(cappedIncreasedAmount))
        _ <- EffectExtra.runAsyncAndForget[G, F, Unit] {
          dataTopic.publish1((player, incrementedData)).void
        }
      } yield ()
    }
}
