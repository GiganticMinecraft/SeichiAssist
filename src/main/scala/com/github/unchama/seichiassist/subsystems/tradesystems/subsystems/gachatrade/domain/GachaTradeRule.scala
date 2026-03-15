package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain

import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaPrizeTableEntry
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.TradeRule

trait GachaTradeRule[ItemStack, TransactionInfo] {
  def ruleFor(
    playerName: String,
    gachaList: Vector[GachaPrizeTableEntry[ItemStack]]
  ): TradeRule[ItemStack, TransactionInfo]
}
