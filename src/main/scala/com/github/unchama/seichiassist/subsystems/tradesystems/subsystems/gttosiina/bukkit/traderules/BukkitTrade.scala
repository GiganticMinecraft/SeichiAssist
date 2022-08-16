package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.bukkit.traderules

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity.Gigantic
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaprizefactory.bukkit.StaticGachaPrizeFactory
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.{
  TradeResult,
  TradeRule,
  TradeSuccessResult
}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object BukkitTrade {

  import cats.implicits._

  def apply[F[_]: ConcurrentEffect](name: String)(
    implicit gachaAPI: GachaAPI[F, ItemStack, Player],
    canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack]
  ): TradeRule[ItemStack] =
    (contents: List[ItemStack]) => {
      val eff = for {
        gachaList <- gachaAPI.list
      } yield {
        val giganticItemStacks = gachaList
          .filter(GachaRarity.of[ItemStack](_) == Gigantic)
          .map(gachaPrize =>
            gachaPrize
              .copy(itemStack = canBeSignedAsGachaPrize.signWith(name)(gachaPrize.itemStack))
          )

        // 交換可能なItemStack達
        val tradableItems = contents.filter { targetItem =>
          giganticItemStacks.exists(gachaPrize => gachaPrize.itemStack.isSimilar(targetItem))
        }

        // 交換不可能なItemStack達
        val nonTradableItems = contents.diff(tradableItems)

        TradeResult[ItemStack](
          tradableItems.map(itemStack =>
            TradeSuccessResult(
              StaticGachaPrizeFactory.getMaxRingo(name),
              itemStack.getAmount * SeichiAssist.seichiAssistConfig.rateGiganticToRingo
            )
          ),
          nonTradableItems
        )
      }
      eff.toIO.unsafeRunSync()
    }
}
