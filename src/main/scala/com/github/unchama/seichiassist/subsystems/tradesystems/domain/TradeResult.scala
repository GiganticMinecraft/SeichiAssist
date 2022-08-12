package com.github.unchama.seichiassist.subsystems.tradesystems.domain

/**
 * ガチャ景品から交換した結果表す型
 */
case class TradeResult[ItemStack](
  tradedSuccessResult: List[TradeSuccessResult[ItemStack]],
  nonTradableItemStacks: List[ItemStack]
)

case class TradeSuccessResult[ItemStack](itemStack: ItemStack, amount: Int) {
  require(amount >= 0)
}
