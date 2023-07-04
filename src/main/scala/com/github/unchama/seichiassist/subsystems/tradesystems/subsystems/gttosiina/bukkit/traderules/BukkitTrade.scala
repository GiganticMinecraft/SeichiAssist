package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.bukkit.traderules

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.{CanBeSignedAsGachaPrize, GachaPrize}
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaRarity.GachaRarity
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaRarity.GachaRarity.Gigantic
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.{
  TradeResult,
  TradeRule,
  TradeSuccessResult
}
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.domain.StaticTradeItemFactory
import org.bukkit.inventory.ItemStack

class BukkitTrade(owner: String, gachaPrizeTable: Vector[GachaPrize[ItemStack]])(
  implicit canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack],
  tradeItemFactory: StaticTradeItemFactory[ItemStack]
) extends TradeRule[ItemStack, Unit] {

  /**
   * プレーヤーが入力したアイテムから、交換結果を計算する
   */
  override def trade(contents: List[ItemStack]): TradeResult[ItemStack, Unit] = {
    val giganticItemStacks = gachaPrizeTable
      .filter(GachaRarity.of[ItemStack](_) == Gigantic)
      .map(gachaPrize => canBeSignedAsGachaPrize.signWith(owner)(gachaPrize))

    val (tradableItems, nonTradableItems) =
      contents.partition(itemStack => giganticItemStacks.exists(_.isSimilar(itemStack)))

    TradeResult[ItemStack, Unit](
      tradableItems.map(_ =>
        /* NOTE: 2022/10/19現在、交換できるギガンティックアイテムは
              スタックできるアイテムではない。
              すなわち、この実装は交換できるアイテムが必ず単一のアイテムである
              ことが前提となっている。
         */
        TradeSuccessResult(
          tradeItemFactory.getMaxRingo(owner),
          SeichiAssist.seichiAssistConfig.rateGiganticToRingo,
          ()
        )
      ),
      nonTradableItems
    )
  }
}
