package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.TradeResult
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules.BigOrRegular

trait GachaTradeAPI[F[_], Player, ItemStack] {

  /**
   * @return プレイヤーが取引可能なアイテムのリストを取得する
   */
  def getTradableItems: Kleisli[F, Player, Vector[ItemStack]]

  /**
   * @return 実際にガチャ景品とガチャ券の取引を行う作用
   */
  def trade(
    contents: List[ItemStack]
  ): Kleisli[F, Player, TradeResult[ItemStack, (BigOrRegular, Int)]]

}
