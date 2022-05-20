package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.application.actions

import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.domain.TradeResult

trait Trade[F[_], ItemStack] {

  /**
   * ガチャアイテムから椎名林檎やガチャ券へ交換できるアイテムを列挙する
   */
  def trade(contents: List[ItemStack]): F[TradeResult[ItemStack]]

}

object Trade {

  def apply[F[_], ItemStack](implicit ev: Trade[F, ItemStack]): Trade[F, ItemStack] = ev

}
