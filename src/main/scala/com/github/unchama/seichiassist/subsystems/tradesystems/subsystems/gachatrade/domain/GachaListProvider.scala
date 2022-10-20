package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain

import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaprize.GachaPrize
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.TradeRule

trait GachaListProvider[F[_], ItemStack] {
  def readGachaList: F[Vector[GachaPrize[ItemStack]]]
}

trait GachaTradeRule[ItemStack] {
  def ruleFor(
    playerName: String,
    gachaList: Vector[GachaPrize[ItemStack]]
  ): TradeRule[ItemStack, Unit]
}
