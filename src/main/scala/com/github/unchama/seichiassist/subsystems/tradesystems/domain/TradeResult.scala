package com.github.unchama.seichiassist.subsystems.tradesystems.domain

/**
 * ガチャ景品から交換した結果表す型
 */
case class TradeResult[ItemStack, TransactionInfo](
  tradedSuccessResult: List[TradeSuccessResult[ItemStack, TransactionInfo]],
  nonTradableItemStacks: List[ItemStack]
)

case class TradeSuccessResult[ItemStack, TransactionInfo](
  itemStack: ItemStack,
  amount: Int,
  transactionInfo: TransactionInfo
) {
  require(amount >= 1)
}
