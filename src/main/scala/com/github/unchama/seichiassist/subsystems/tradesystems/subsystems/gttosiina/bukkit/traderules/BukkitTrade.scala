package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.bukkit.traderules

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.factories.BukkitStaticGachaPrizeFactory
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaRarity.GachaRarity.Gigantic
import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaprize.GachaPrize
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.{
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
    val giganticItemStacks = gachaPrizeTable
      .filter(GachaRarity.of[ItemStack](_) == Gigantic)
      .map(gachaPrize => canBeSignedAsGachaPrize.signWith(owner)(gachaPrize))

    val (tradableItems, nonTradableItems) =
      contents.partition(itemStack => giganticItemStacks.exists(_.isSimilar(itemStack)))

    TradeResult[ItemStack](
      tradableItems.map(_ =>
        /* NOTE: 2022/08/16現在、交換できるあたり、大当たりのアイテムは
              スタックできるアイテムではない。
              すなわち、この実装は交換できるアイテムが必ず単一のアイテムである
              ことが前提となっている。
         */
        TradeSuccessResult(
          BukkitStaticGachaPrizeFactory.getMaxRingo(owner),
          SeichiAssist.seichiAssistConfig.rateGiganticToRingo
        )
      ),
      nonTradableItems
    )
  }
}
