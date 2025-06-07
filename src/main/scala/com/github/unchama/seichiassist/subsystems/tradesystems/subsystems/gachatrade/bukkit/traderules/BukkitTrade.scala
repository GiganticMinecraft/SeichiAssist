package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules

import cats.effect.IO
import com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.factories.BukkitGachaSkullData
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaRarity.GachaRarity
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaRarity.GachaRarity._
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.{
  CanBeSignedAsGachaPrize,
  GachaPrizeTableEntry
}
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.{
  TradeResult,
  TradeRule,
  TradeSuccessResult
}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

sealed trait BigOrRegular

object BigOrRegular {

  case object Big extends BigOrRegular

  case object Regular extends BigOrRegular

}

class BukkitTrade(owner: String, gachaPrizeTable: Vector[GachaPrizeTableEntry[ItemStack]])(
  implicit canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack],
  playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
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

    // NOTE: ガチャ景品の交換は耐久値を無視する必要があるため、緩い判定を行うために独自の比較関数を用意する
    //  ref: https://github.com/GiganticMinecraft/SeichiAssist/issues/2150
    def compareItemStack(leftHand: ItemStack, rightHand: ItemStack): Boolean = {
      if (leftHand.getType != rightHand.getType) return false

      val leftMeta = leftHand.getItemMeta
      val rightMeta = rightHand.getItemMeta

      val leftLore = Option(leftMeta.getLore)
      val rightLore = Option(rightMeta.getLore)

      val leftEnchantments = leftMeta.getEnchants
      val rightEnchantments = rightMeta.getEnchants

      val isSameDisplayName =
        leftMeta.hasDisplayName && rightMeta.hasDisplayName
      val isSameLore = leftLore == rightLore
      val isSameEnchantments = leftEnchantments == rightEnchantments

      isSameDisplayName && isSameLore && isSameEnchantments
    }

    val (nonTradable, tradable) =
      contents.partitionMap { itemStack =>
        if (bigList.exists(bigItem => compareItemStack(bigItem, itemStack)))
          Right(BigOrRegular.Big -> itemStack.getAmount)
        else if (regularList.exists(regularItem => compareItemStack(regularItem, itemStack)))
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
