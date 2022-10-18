package com.github.unchama.seichiassist.subsystems.gacha.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.LotteryOfGachaItems

import scala.annotation.tailrec

class LotteryOfGachaItems[F[_]: Sync, ItemStack](
  implicit staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack]
) extends LotteryOfGachaItems[F, ItemStack] {

  import cats.implicits._

  def runLottery(
    amount: Int,
    gachaPrizesListRepository: Ref[F, Vector[GachaPrize[ItemStack]]]
  ): F[Vector[GachaPrize[ItemStack]]] =
    for {
      gachaPrizes <- gachaPrizesListRepository.get
      randomList <-
        (0 until amount).toList.traverse(_ => Sync[F].delay(Math.random()))
    } yield randomList
      .map(random => lottery(GachaProbability(1.0), GachaProbability(random), gachaPrizes))
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
    gachaPrizes: Vector[GachaPrize[ItemStack]]
  ): GachaPrize[ItemStack] = {
    if (gachaPrizes.isEmpty) {
      GachaPrize(
        staticGachaPrizeFactory.gachaRingo,
        GachaProbability(1.0),
        signOwner = false,
        GachaPrizeId(0)
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
