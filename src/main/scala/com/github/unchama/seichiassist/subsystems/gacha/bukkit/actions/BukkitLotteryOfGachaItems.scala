package com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.LotteryOfGachaItems
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  GachaPrize,
  GachaPrizeId,
  GachaProbability
}
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaprizefactory.bukkit.StaticGachaPrizeFactory
import org.bukkit.inventory.ItemStack

import scala.annotation.tailrec

class BukkitLotteryOfGachaItems[F[_]: Sync] extends LotteryOfGachaItems[F, ItemStack] {

  import cats.implicits._

  def runLottery(
    amount: Int,
    gachaPrizesListRepository: Ref[F, Vector[GachaPrize[ItemStack]]]
  ): F[Vector[GachaPrize[ItemStack]]] =
    for {
      gachaPrizes <- gachaPrizesListRepository.get
      randomList <-
        (0 until amount)
          .map(_ => Sync[F].delay(Math.random()))
          .toList
          .traverse(random => random)
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
    val nowSum = sumGachaProbability.value - gachaPrizes.head.probability.value
    val droppedGachaPrizes = gachaPrizes.drop(1)
    if (nowSum <= probability.value) gachaPrizes.head
    else if (droppedGachaPrizes.nonEmpty)
      lottery(GachaProbability(nowSum), probability, droppedGachaPrizes)
    else
      GachaPrize(
        StaticGachaPrizeFactory.gachaRingo,
        GachaProbability(1.0),
        hasOwner = false,
        GachaPrizeId(0)
      )
  }

}
