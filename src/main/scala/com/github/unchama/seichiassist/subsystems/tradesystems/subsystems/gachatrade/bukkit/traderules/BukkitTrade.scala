package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity._
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaskull.bukkit.GachaSkullData
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.{
  TradeResult,
  TradeRule,
  TradeSuccessResult
}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object BukkitTrade {

  import cats.implicits._

  def apply[F[_]: ConcurrentEffect](owner: String)(
    implicit gachaAPI: GachaAPI[F, ItemStack, Player],
    canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack]
  ): TradeRule[ItemStack] =
    (contents: List[ItemStack]) => {
      val eff = for {
        gachaList <- gachaAPI.list
      } yield {
        // GTアイテムを除去し、今回の対象であるあたりまでを含めたリスト
        val targetsList =
          gachaList
            .filterNot(_.probability.value < Gigantic.maxProbability.value)
            .filter(_.probability.value < Regular.maxProbability.value)

        // 大当たりのアイテム
        val bigList = targetsList
          .filter(_.probability.value < Big.maxProbability.value)
          .map(gachaPrize =>
            gachaPrize
              .copy(itemStack = canBeSignedAsGachaPrize.signWith(owner)(gachaPrize.itemStack))
          )

        // あたりのアイテム
        val regularList = targetsList.diff(bigList).map { gachaPrize =>
          canBeSignedAsGachaPrize.signWith(owner)(gachaPrize.itemStack)
        }

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
            TradeSuccessResult(GachaSkullData.gachaForExchanging, itemStack.getAmount * 12)
          ) ++ tradableRegularItems.map(itemStack =>
            TradeSuccessResult(GachaSkullData.gachaForExchanging, itemStack.getAmount * 3)
          ),
          nonTradableItems
        )
      }
      eff.toIO.unsafeRunSync()
    }

}
