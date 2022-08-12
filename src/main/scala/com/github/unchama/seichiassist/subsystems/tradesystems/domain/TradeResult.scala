package com.github.unchama.seichiassist.subsystems.tradesystems.domain

/**
 * ガチャ景品から交換した結果表す型
 * @param tradedAmounts 交換後のアイテムの数
 * @param nonTradableItemStacks 交換できなかったItemStack
 */
case class TradeResult[ItemStack](
  tradedAmounts: List[TradedAmount],
  nonTradableItemStacks: List[ItemStack]
)

case class TradedAmount(amount: Int) {
  require(amount > 0)
}
