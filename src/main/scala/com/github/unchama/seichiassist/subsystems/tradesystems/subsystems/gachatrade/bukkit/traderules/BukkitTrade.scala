package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.generic.ListExtra
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity._
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaskull.bukkit.GachaSkullData
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.{
  BigOrRegular,
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
        // 大当たりのアイテム
        val bigList = gachaList.filter(GachaRarity.of[ItemStack](_) == Big).map { gachaPrize =>
          canBeSignedAsGachaPrize.signWith(owner)(gachaPrize)
        }

        // あたりのアイテム
        val regularList = gachaList.filter(GachaRarity.of[ItemStack](_) == Regular).map {
          gachaPrize => canBeSignedAsGachaPrize.signWith(owner)(gachaPrize)
        }

        val (nonTradable, tradable) =
          ListExtra.partitionWith(contents) { itemStack =>
            if (bigList.exists(_.isSimilar(itemStack)))
              Right(BigOrRegular.Big)
            else if (regularList.exists(_.isSimilar(itemStack)))
              Right(BigOrRegular.Regular)
            else Left(itemStack)
          }

        /* NOTE: 2022/08/16現在、交換できるギガンテックアイテムは
            スタックできるアイテムではない。
            すなわち、この実装は交換できるアイテムが必ず単一のアイテムである
            ことが前提となっている。
         */
        TradeResult[ItemStack](
          tradable.map {
            case BigOrRegular.Big => TradeSuccessResult(GachaSkullData.gachaForExchanging, 12)
            case BigOrRegular.Regular =>
              TradeSuccessResult(GachaSkullData.gachaForExchanging, 3)
          },
          nonTradable
        )
      }
      eff.toIO.unsafeRunSync()
    }

}
