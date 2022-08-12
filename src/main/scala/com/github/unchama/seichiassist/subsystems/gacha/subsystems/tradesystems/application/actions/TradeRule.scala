package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.application.actions

import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.domain.TradeResult

trait TradeRule[F[_], ItemStack] {

  /**
   * ガチャアイテムから椎名林檎やガチャ券へ交換できるアイテムを列挙する
   */
  def trade(contents: List[ItemStack]): F[TradeResult[ItemStack]]

}

object TradeRule {

  def apply[F[_], ItemStack](implicit ev: TradeRule[F, ItemStack]): TradeRule[F, ItemStack] = ev

}
