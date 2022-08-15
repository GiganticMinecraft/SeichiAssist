package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.bukkit.actions

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity.Gigantic
import com.github.unchama.seichiassist.subsystems.tradesystems.application.actions.TradeRule
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.{TradeResult, TradeSuccessResult}
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
        giganticItemStacks = gachaList // TODO GTアイテムかどうかを確率に依存すべきではない
          .filter(GachaRarity.of[ItemStack](_) == Gigantic)
          .map(gachaPrize =>
            gachaPrize
              .copy(itemStack = canBeSignedAsGachaPrize.signWith(name)(gachaPrize.itemStack))
          )
      } yield {
        // 交換可能なItemStack達
        val tradableItems = contents.filter { targetItem =>
          giganticItemStacks.exists(gachaPrize => gachaPrize.itemStack.isSimilar(targetItem))
        }

        // 交換不可能なItemStack達
        val nonTradableItems = contents.diff(tradableItems)

        TradeResult[ItemStack](
          tradableItems.map(itemStack =>
            TradeSuccessResult(
              itemStack,
              itemStack.getAmount * SeichiAssist.seichiAssistConfig.rateGiganticToRingo
            )
          ),
          nonTradableItems
        )
      }
      eff.toIO.unsafeRunSync()
    }
}
