package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.TradeResult
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules.BigOrRegular
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain.TradeError

trait GachaTradeAPI[F[_], Player, ItemStack] {

  /**
   * @return プレイヤーが取引可能なアイテムのリストを取得する
   */
  def getTradableItems: Kleisli[F, Player, Vector[ItemStack]]

  /**
   * @return インベントリから実際にガチャ景品とガチャ券の取引を行う作用
   */
  def tradeFromInventory(
    contents: List[ItemStack]
  ): Kleisli[F, Player, TradeResult[ItemStack, (BigOrRegular, Int)]]

  /**
   * @return `mineStackObject` を `amount` だけ取引を行うことを試みる作用
   */
  def tryTradeFromMineStack(
    player: Player,
    mineStackObject: MineStackObject[ItemStack],
    amount: Int
  ): F[Either[TradeError, TradeResult[ItemStack, (BigOrRegular, Int)]]]

}
