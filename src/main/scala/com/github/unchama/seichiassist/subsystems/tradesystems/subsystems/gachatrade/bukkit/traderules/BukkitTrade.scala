package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules

import com.github.unchama.generic.ListExtra
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.factories.BukkitGachaSkullData
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity._
import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaprize.GachaPrize
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.{
  TradeResult,
  TradeRule,
  TradeSuccessResult
}
import org.bukkit.inventory.ItemStack

private sealed trait BigOrRegular

object BigOrRegular {

  case object Big extends BigOrRegular

  case object Regular extends BigOrRegular

}

class BukkitTrade(owner: String, gachaPrizeTable: Vector[GachaPrize[ItemStack]])(
  implicit canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack]
) extends TradeRule[ItemStack] {

  /**
   * プレーヤーが入力したアイテムから、交換結果を計算する
   */
  override def trade(contents: List[ItemStack]): TradeResult[ItemStack] = {
    // 大当たりのアイテム
    val bigList = gachaPrizeTable.filter(GachaRarity.of[ItemStack](_) == Big).map {
      gachaPrize => canBeSignedAsGachaPrize.signWith(owner)(gachaPrize)
    }

    // あたりのアイテム
    val regularList = gachaPrizeTable.filter(GachaRarity.of[ItemStack](_) == Regular).map {
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
        case BigOrRegular.Big => TradeSuccessResult(BukkitGachaSkullData.gachaForExchanging, 12)
        case BigOrRegular.Regular =>
          TradeSuccessResult(BukkitGachaSkullData.gachaForExchanging, 3)
      },
      nonTradable
    )
  }

}
