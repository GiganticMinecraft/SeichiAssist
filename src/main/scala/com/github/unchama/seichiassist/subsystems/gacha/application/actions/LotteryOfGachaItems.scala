package com.github.unchama.seichiassist.subsystems.gacha.application.actions

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit.GachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  GachaPrizeId,
  GachaPrizesDataOperations,
  GachaProbability
}
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaprizefactory.bukkit.StaticGachaPrizeFactory

/**
 * ガチャアイテムの抽選を行う作用
 */

trait LotteryOfGachaItems[F[_]] {

  def lottery: F[Vector[GachaPrize]] = lottery(1)

  def lottery(amount: Int): F[Vector[GachaPrize]]

}

object LotteryOfGachaItems {

  def apply[F[_]](implicit ev: LotteryOfGachaItems[F]): LotteryOfGachaItems[F] =
    ev

  import cats.implicits._

  def using[F[_]: Sync](
    implicit gachaPrizesDataOperations: GachaPrizesDataOperations[F]
  ): LotteryOfGachaItems[F] = (amount: Int) =>
    for {
      gachaPrizes <- gachaPrizesDataOperations.gachaPrizesList
    } yield {
      (0 until amount).map { _ =>
        val random = Math.random()

        def getGachaPrize: GachaPrize = {
          gachaPrizes.foldLeft(1.0) { (sum, gachaPrize) =>
            val nowSum = sum - gachaPrize.probability.value
            if (nowSum <= random) return gachaPrize
            else nowSum
          }
          GachaPrize(
            StaticGachaPrizeFactory.gachaRingo,
            GachaProbability(1.0),
            hasOwner = false,
            GachaPrizeId(0)
          )
        }

        getGachaPrize
      }.toVector
    }
}
