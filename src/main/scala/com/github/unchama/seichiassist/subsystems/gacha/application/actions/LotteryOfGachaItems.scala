package com.github.unchama.seichiassist.subsystems.gacha.application.actions

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit.GachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  GachaPrizeId,
  GachaPrizesDataOperations
}
import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory

/**
 * ガチャを抽選を行う作用を返すtrait
 */

trait LotteryOfGachaItems[F[_]] {

  def draw: F[Vector[GachaPrize]] = draw(1)

  def draw(amount: Int): F[Vector[GachaPrize]]

}

object LotteryOfGachaItems {

  def apply[F[_]](implicit ev: LotteryOfGachaItems[F]): LotteryOfGachaItems[F] =
    ev

  import cats.implicits._

  def using[F[_]: Sync](
    implicit gachaPrizesDataOperations: GachaPrizesDataOperations[F]
  ): LotteryOfGachaItems[F] = (amount: Int) =>
    for {
      gachaPrizes <- gachaPrizesDataOperations.getGachaPrizesList
    } yield {
      (0 until amount).map { _ =>
        val random = Math.random()

        def getGachaPrize: GachaPrize = {
          gachaPrizes.foldLeft(1.0) { (sum, gachaPrize) =>
            val nowSum = sum - gachaPrize.probability
            if (nowSum <= random) return gachaPrize
            else nowSum
          }
          GachaPrize(
            StaticGachaPrizeFactory.getGachaRingo,
            1.0,
            isAppendOwner = false,
            GachaPrizeId(0)
          )
        }

        getGachaPrize
      }.toVector
    }
}
