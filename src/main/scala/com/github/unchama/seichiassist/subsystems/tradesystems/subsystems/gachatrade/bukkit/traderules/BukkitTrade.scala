package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules

import com.github.unchama.generic.ListExtra
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity._
import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaprize.GachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaskull.bukkit.GachaSkullData
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.{
  BigOrRegular,
  TradeResult,
  TradeRule,
  TradeSuccessResult
}
import org.bukkit.inventory.ItemStack

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
        case BigOrRegular.Big => TradeSuccessResult(GachaSkullData.gachaForExchanging, 12)
        case BigOrRegular.Regular =>
          TradeSuccessResult(GachaSkullData.gachaForExchanging, 3)
      },
      nonTradable
    )
  }

}
