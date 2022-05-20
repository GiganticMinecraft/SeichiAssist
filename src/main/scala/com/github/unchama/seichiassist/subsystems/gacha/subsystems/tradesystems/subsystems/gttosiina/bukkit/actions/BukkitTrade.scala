package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.subsystems.gttosiina.bukkit.actions

import cats.effect.Sync
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizesDataOperations
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.application.actions.Trade
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.domain.{
  TradeResult,
  TradedAmount
}
import org.bukkit.inventory.ItemStack

object BukkitTrade {

  import cats.implicits._

  def apply[F[_]: Sync](name: String)(
    implicit gachaPrizesDataOperations: GachaPrizesDataOperations[F]
  ): Trade[F, ItemStack] = (contents: List[ItemStack]) =>
    for {
      gachaList <- gachaPrizesDataOperations.gachaPrizesList
    } yield {
      // TODO GTアイテムかどうかを確率に依存すべきではない
      val giganticItemStacks =
        gachaList.filter(_.probability.value < 0.001).map(_.createNewItem(Some(name)))

      // 交換可能なItemStack達
      val tradableItems = contents.filter { targetItem =>
        giganticItemStacks.exists(_.isSimilar(targetItem))
      }

      // 交換不可能なItemStack達
      val nonTradableItems = contents.diff(tradableItems)

      TradeResult[ItemStack](
        tradableItems.map(itemStack => TradedAmount(itemStack.getAmount)),
        nonTradableItems
      )
    }

}
