package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.subsystems.gachatrade.bukkit.actions

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity._
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.application.actions.Trade
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.domain.{
  TradeResult,
  TradedAmount
}
import org.bukkit.inventory.ItemStack

object BukkitTrade {

  import cats.implicits._

  def apply[F[_]: ConcurrentEffect](
    owner: String
  )(implicit gachaAPI: GachaAPI[F, ItemStack]): Trade[F, ItemStack] =
    (contents: List[ItemStack]) =>
      for {
        gachaList <- gachaAPI.list
        // GTアイテムを除去し、今回の対象であるあたりまでを含めたリスト
        targetsList =
          gachaList
            .filterNot(_.probability.value < Gigantic.maxProbability.value)
            .filter(_.probability.value < Regular.maxProbability.value)

        // 大当たりのアイテム
        bigList <- targetsList
          .filter(_.probability.value < Big.maxProbability.value)
          .traverse(gachaPrize =>
            gachaAPI.grantGachaPrize(gachaPrize).createNewItem(Some(owner))
          )

        // あたりのアイテム
        regularList <- targetsList.diff(bigList).traverse { gachaPrize =>
          gachaAPI.grantGachaPrize(gachaPrize).createNewItem(Some(owner))
        }

      } yield {
        // 交換可能な大当たりのアイテム
        val tradableBigItems =
          contents.filter(targetItem =>
            bigList.exists(itemStack => itemStack.isSimilar(targetItem))
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

}
