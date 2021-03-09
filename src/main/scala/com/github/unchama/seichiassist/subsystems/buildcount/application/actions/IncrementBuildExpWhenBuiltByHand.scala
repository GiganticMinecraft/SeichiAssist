package com.github.unchama.seichiassist.subsystems.buildcount.application.actions

import cats.Monad
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.Diff
import com.github.unchama.generic.ratelimiting.RateLimiter
import com.github.unchama.minecraft.actions.SendMinecraftMessage
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

  def apply[
    F[_], Player
  ](implicit ev: IncrementBuildExpWhenBuiltByHand[F, Player]): IncrementBuildExpWhenBuiltByHand[F, Player] = ev

  def using[
    F[_]
    : Monad
    : ClassifyPlayerWorld[*[_], Player]
    : SendMinecraftMessage[*[_], Player],
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
        levelDiff <- dataRepository(player).modify { oldAmount =>
          val newAmount = oldAmount.modifyExpAmount(_.add(amountToIncrement))
          (newAmount, Diff.fromValues(oldAmount.levelCorrespondingToExp, newAmount.levelCorrespondingToExp))
        }
        _ <- levelDiff.traverse(LevelUpNotifier[F, Player].notifyTo(player))
      } yield ()
    }
}
