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
          .map(gachaPrize => canBeSignedAsGachaPrize.signWith(name)(gachaPrize))

        val nonTradableItems =
          contents.filter(itemStack => giganticItemStacks.exists(_.isSimilar(itemStack)))

        TradeResult[ItemStack](
          contents
            .diff(nonTradableItems)
            .map(_ =>
              /* NOTE: 2022/08/16現在、交換できるあたり、大当たりのアイテムは
                  スタックできるアイテムではない。
                  すなわち、この実装は交換できるアイテムが必ず単一のアイテムである
                  ことが前提となっている。
               */
              TradeSuccessResult(
                StaticGachaPrizeFactory.getMaxRingo(name),
                SeichiAssist.seichiAssistConfig.rateGiganticToRingo
              )
            ),
          nonTradableItems
        )
      }
      eff.toIO.unsafeRunSync()
    }
}
