package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.actions

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity._
import com.github.unchama.seichiassist.subsystems.tradesystems.application.actions.TradeRule
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.{
  TradeResult,
  TradeSuccessResult
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
            .filterNot(GachaRarity.of[ItemStack](_) == Gigantic)
            .filter(GachaRarity.of[ItemStack](_) == Regular)

        // 大当たりのアイテム
        bigList = targetsList
          .filter(GachaRarity.of[ItemStack](_) == Big)
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
            TradeSuccessResult(itemStack, itemStack.getAmount * 12)
          ) ++ tradableRegularItems.map(itemStack =>
            TradeSuccessResult(itemStack, itemStack.getAmount * 3)
          ),
          nonTradableItems
        )
      }
      eff.toIO.unsafeRunSync()
    }

}
