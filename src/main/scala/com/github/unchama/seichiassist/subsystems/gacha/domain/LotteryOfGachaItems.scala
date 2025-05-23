package com.github.unchama.seichiassist.subsystems.gacha.domain

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.{
  GachaPrizeId,
  GachaPrizeTableEntry,
  GachaProbability,
  StaticGachaPrizeFactory
}
import com.github.unchama.generic.Cloneable

import scala.annotation.tailrec

class LotteryOfGachaItems[F[_]: Sync, ItemStack: Cloneable](
  implicit staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack]
) {

  import cats.implicits._

  def runLottery(
    amount: Int,
    gachaPrizesListRepository: Vector[GachaPrizeTableEntry[ItemStack]]
  ): F[Vector[GachaPrizeTableEntry[ItemStack]]] =
    for {
      randomList <-
        (0 until amount).toList.traverse(_ => Sync[F].delay(Math.random()))
    } yield randomList
      .map(random =>
        lottery(GachaProbability(1.0), GachaProbability(random), gachaPrizesListRepository)
      )
      .toVector

  /**
   * ガチャアイテムの抽選を行うための再帰関数
   *
   * @param sumGachaProbability 現在の合計値
   * @param probability ガチャの確率
   * @param gachaPrizes ガチャの景品リスト
   * @return 抽選されたガチャアイテム
   */
  @tailrec
  private def lottery(
    sumGachaProbability: GachaProbability,
    probability: GachaProbability,
    gachaPrizes: Vector[GachaPrizeTableEntry[ItemStack]]
  ): GachaPrizeTableEntry[ItemStack] = {
    if (gachaPrizes.isEmpty) {
      GachaPrizeTableEntry(
        staticGachaPrizeFactory.gachaRingo,
        GachaProbability(1.0),
        signOwner = false,
        GachaPrizeId(0),
        None
      )
    } else {
      val prizeAtHead = gachaPrizes.head
      val probabilitySumUptoHead = sumGachaProbability.value - prizeAtHead.probability.value
      if (probabilitySumUptoHead <= probability.value) {
        prizeAtHead
      } else {
        lottery(GachaProbability(probabilitySumUptoHead), probability, gachaPrizes.drop(1))
      }
    }
  }

}
