package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade

import cats.data.Kleisli

trait GachaTradeAPI[F[_], Player, ItemStack] {

  /**
   * @return プレイヤーが取引可能なアイテムのリストを取得する
   */
  def getTradableItems: Kleisli[F, Player, Vector[ItemStack]]

}
