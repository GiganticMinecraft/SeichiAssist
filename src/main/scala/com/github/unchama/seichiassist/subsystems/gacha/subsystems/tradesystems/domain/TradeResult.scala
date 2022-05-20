package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.domain

/**
 * ガチャ景品から交換した結果表す型
 * @param tradableItemStacks 交換したItemStackと交換後のアイテムの数
 * @param nonTradableItemStacks 交換できなかったItemStack
 */
case class TradeResult[ItemStack](
  tradableItemStacks: Map[ItemStack, tradedAmount],
  nonTradableItemStacks: List[ItemStack]
)

case class tradedAmount(amount: Int) {
  require(amount > 0)
}
