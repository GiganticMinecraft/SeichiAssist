package com.github.unchama.buildassist.application.actions

import cats.Monad
import cats.effect.concurrent.Ref
import com.github.unchama.buildassist.application.BuildExpMultiplier
import com.github.unchama.buildassist.domain.explevel.BuildExpAmount
import com.github.unchama.buildassist.domain.playerdata.BuildAmountData
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.ratelimiting.RateLimiter

/**
 * [[Player]] が手でブロックを設置した際に建築量を加算するアクションを提供する型クラス。
 */
trait IncrementBuildExpWhenBuiltByHand[F[_], Player] {

  def of(player: Player): F[Unit] = of(player, BuildExpAmount(1))

  def of(player: Player, by: BuildExpAmount): F[Unit]

}

object IncrementBuildExpWhenBuiltByHand {

  import cats.implicits._

  def apply[
    F[_], Player
  ](implicit ev: IncrementBuildExpWhenBuiltByHand[F, Player]): IncrementBuildExpWhenBuiltByHand[F, Player] = ev

  def using[
    F[_] : Monad : ClassifyPlayerWorld[*[_], Player],
    Player
  ](rateLimiterRepository: KeyedDataRepository[Player, RateLimiter[F, BuildExpAmount]],
    dataRepository: KeyedDataRepository[Player, Ref[F, BuildAmountData]])
   (implicit multiplier: BuildExpMultiplier): IncrementBuildExpWhenBuiltByHand[F, Player] =
    (player: Player, by: BuildExpAmount) => {
      val F: Monad[F] = Monad[F]

      for {
        amountToRequestIncrement <-
          F.ifM(ClassifyPlayerWorld[F, Player].isInBuildWorld(player))(
            F.ifF(ClassifyPlayerWorld[F, Player].isInSeichiWorld(player))(
              by.mapAmount(_ * multiplier.whenInSeichiWorld),
              by
            ),
            F.pure(BuildExpAmount(0))
          )
        amountToIncrement <-
          rateLimiterRepository(player).requestPermission(amountToRequestIncrement)
        _ <-
          dataRepository(player)
            .update(_.modifyExpAmount(_.add(amountToIncrement)))
      } yield ()
    }
}
