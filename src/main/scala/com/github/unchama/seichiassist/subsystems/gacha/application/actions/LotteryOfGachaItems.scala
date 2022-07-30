package com.github.unchama.seichiassist.subsystems.gacha.application.actions

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit.GachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.{GachaPrizeId, GachaProbability}
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaprizefactory.bukkit.StaticGachaPrizeFactory

import scala.annotation.tailrec

/**
 * ガチャアイテムの抽選を行う作用
 */

class LotteryOfGachaItems[F[_]: Sync] {

  import cats.implicits._

  def runLottery(
    amount: Int,
    gachaPrizesListRepository: Ref[F, Vector[GachaPrize]]
  ): F[Vector[GachaPrize]] =
    for {
      gachaPrizes <- gachaPrizesListRepository.get
      randomList <-
        (0 until amount)
          .map(_ => Sync[F].delay(Math.random()))
          .toList
          .traverse(random => random)
    } yield randomList.map(lottery(1.0, _, gachaPrizes)).toVector

  /**
   * ガチャアイテムの抽選を行うための再帰関数
   *
   * @param sum 現在の合計値
   * @param random 乱数(1.0まで)
   * @param gachaPrizes ガチャの景品リスト
   * @return 抽選されたガチャアイテム
   */
  @tailrec
  private def lottery(
    sum: Double,
    random: Double,
    gachaPrizes: Vector[GachaPrize]
  ): GachaPrize = {
    val nowSum = sum - gachaPrizes.head.probability.value
    val droppedGachaPrizes = gachaPrizes.drop(1)
    if (nowSum <= random) gachaPrizes.head
    else if (droppedGachaPrizes.nonEmpty) lottery(nowSum, random, droppedGachaPrizes)
    else
      GachaPrize(
        StaticGachaPrizeFactory.gachaRingo,
        GachaProbability(1.0),
        hasOwner = false,
        GachaPrizeId(0)
      )
  }

}
