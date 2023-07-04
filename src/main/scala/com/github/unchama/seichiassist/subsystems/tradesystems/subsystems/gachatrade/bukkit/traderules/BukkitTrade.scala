package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules

import com.github.unchama.generic.ListExtra
import com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.factories.BukkitGachaSkullData
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaRarity.GachaRarity
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaRarity.GachaRarity._
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.{CanBeSignedAsGachaPrize, GachaPrize}
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.{
  TradeResult,
  TradeRule,
  TradeSuccessResult
}
import org.bukkit.inventory.ItemStack

sealed trait BigOrRegular

object BigOrRegular {

  case object Big extends BigOrRegular

  case object Regular extends BigOrRegular

}

class BukkitTrade(owner: String, gachaPrizeTable: Vector[GachaPrize[ItemStack]])(
  implicit canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack]
) extends TradeRule[ItemStack, (BigOrRegular, Int)] {

  /**
   * プレーヤーが入力したアイテムから、交換結果を計算する
   */
  override def trade(contents: List[ItemStack]): TradeResult[ItemStack, (BigOrRegular, Int)] = {
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
          Right(BigOrRegular.Big -> itemStack.getAmount)
        else if (regularList.exists(_.isSimilar(itemStack)))
          Right(BigOrRegular.Regular -> itemStack.getAmount)
        else Left(itemStack)
      }

    TradeResult[ItemStack, (BigOrRegular, Int)](
      tradable.map {
        case (BigOrRegular.Big, amount) =>
          TradeSuccessResult(
            BukkitGachaSkullData.gachaForExchanging,
            12 * amount,
            (BigOrRegular.Big, amount)
          )
        case (BigOrRegular.Regular, amount) =>
          TradeSuccessResult(
            BukkitGachaSkullData.gachaForExchanging,
            3 * amount,
            (BigOrRegular.Regular, amount)
          )
      },
      nonTradable
    )
  }

}
