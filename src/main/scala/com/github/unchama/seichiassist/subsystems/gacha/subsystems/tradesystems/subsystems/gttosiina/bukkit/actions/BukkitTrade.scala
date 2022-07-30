package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.subsystems.gttosiina.bukkit.actions

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
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
      } yield {
        // TODO GTアイテムかどうかを確率に依存すべきではない
        val giganticItemStacks =
          gachaList
            .filter(_.probability.value < Gigantic.maxProbability.value)
            .traverse(gachaPrize =>
              gachaAPI.grantGachaPrize(gachaPrize).createNewItem(Some(name))
            )

        // 交換可能なItemStack達
        val tradableItems = contents.filter { targetItem =>
          giganticItemStacks
            .map { itemStacks =>
              itemStacks.exists(gachaPrizeItemStack =>
                gachaPrizeItemStack.isSimilar(targetItem)
              )
            }
            .toIO
            .unsafeRunSync()
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
