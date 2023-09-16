package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain

import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaPrizeTableEntry
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.TradeRule
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules.BigOrRegular

trait GachaTradeRule[ItemStack] {
  def ruleFor(
    playerName: String,
    gachaList: Vector[GachaPrizeTableEntry[ItemStack]]
  ): TradeRule[ItemStack, (BigOrRegular, Int)]
}
