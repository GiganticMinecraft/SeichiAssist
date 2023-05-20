package com.github.unchama.seichiassist.subsystems.tradesystems.domain

/**
 * 特定のアイテムを交換するためのルール（何を何個入れると何が何個得られて何が返却されるか）
 * を定めるオブジェクトの trait
 */
trait TradeRule[ItemStack, TransactionInfo] {

  /**
   * プレーヤーが入力したアイテムから、交換結果を計算する
   */
  def trade(contents: List[ItemStack]): TradeResult[ItemStack, TransactionInfo]

}
