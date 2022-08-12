package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.subsystems.gachatrade.bukkit.actions

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity._
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.application.actions.TradeRule
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.domain.{
  TradeResult,
  TradedAmount
}
import org.bukkit.inventory.ItemStack

object BukkitTrade {

  import cats.implicits._

  def apply[F[_]: ConcurrentEffect](owner: String)(
    implicit gachaAPI: GachaAPI[F, ItemStack],
    canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack]
  ): TradeRule[ItemStack] =
    (contents: List[ItemStack]) => {
      val eff = for {
        gachaList <- gachaAPI.list
        // GTアイテムを除去し、今回の対象であるあたりまでを含めたリスト
        targetsList =
          gachaList
            .filterNot(_.probability.value < Gigantic.maxProbability.value)
            .filter(_.probability.value < Regular.maxProbability.value)

        // 大当たりのアイテム
        bigList = targetsList
          .filter(_.probability.value < Big.maxProbability.value)
          .map(gachaPrize =>
            gachaPrize
              .copy(itemStack = canBeSignedAsGachaPrize.signWith(owner)(gachaPrize.itemStack))
          )

        // あたりのアイテム
        regularList = targetsList.diff(bigList).map { gachaPrize =>
          canBeSignedAsGachaPrize.signWith(owner)(gachaPrize.itemStack)
        }

      } yield {
        // 交換可能な大当たりのアイテム
        val tradableBigItems =
          contents.filter(targetItem =>
            bigList.exists(gachaPrize => gachaPrize.itemStack.isSimilar(targetItem))
          )

        // 交換可能なあたりのアイテム
        val tradableRegularItems = contents.filter(targetItem =>
          regularList.exists(itemStack => itemStack.isSimilar(targetItem))
        )

        // 交換不可能なアイテム達
        val nonTradableItems = contents.diff(tradableBigItems :: tradableRegularItems)

        TradeResult[ItemStack](
          tradableBigItems.map(itemStack =>
            TradedAmount(itemStack.getAmount * 12)
          ) ++ tradableRegularItems.map(itemStack => TradedAmount(itemStack.getAmount * 3)),
          nonTradableItems
        )
      }
      eff.toIO.unsafeRunSync()
    }

}
