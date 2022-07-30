package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.subsystems.gttosiina.bukkit.actions

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity.Gigantic
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.application.actions.Trade
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.domain.{
  TradeResult,
  TradedAmount
}
import org.bukkit.inventory.ItemStack

object BukkitTrade {

  import cats.implicits._

  def apply[F[_]: ConcurrentEffect](
    name: String
  )(implicit gachaAPI: GachaAPI[F, ItemStack]): Trade[F, ItemStack] =
    (contents: List[ItemStack]) =>
      for {
        gachaList <- gachaAPI.list
        giganticItemStacks <- gachaList // TODO GTアイテムかどうかを確率に依存すべきではない
          .filter(_.probability.value < Gigantic.maxProbability.value)
          .traverse(gachaPrize =>
            gachaAPI.grantGachaPrize(gachaPrize).createNewItem(Some(name))
          )
      } yield {
        // 交換可能なItemStack達
        val tradableItems = contents.filter { targetItem =>
          giganticItemStacks.exists(gachaPrizeItemStack =>
            gachaPrizeItemStack.isSimilar(targetItem)
          )
        }

        // 交換不可能なItemStack達
        val nonTradableItems = contents.diff(tradableItems)

        TradeResult[ItemStack](
          tradableItems.map(itemStack =>
            TradedAmount(
              itemStack.getAmount * SeichiAssist.seichiAssistConfig.rateGiganticToRingo
            )
          ),
          nonTradableItems
        )
      }
}
