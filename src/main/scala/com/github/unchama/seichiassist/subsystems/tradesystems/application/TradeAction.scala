package com.github.unchama.seichiassist.subsystems.tradesystems.application

import com.github.unchama.seichiassist.subsystems.tradesystems.domain.TradeRule
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.TradeResult
import cats.effect.Sync

trait TradeAction[F[_], Player, ItemStack, TransactionInfo] {

  protected implicit val F: Sync[F]

  import cats.implicits._

  /**
   * @return 取引結果に基づき、アイテム数調整などを行う作用
   */
  protected def applyTradeResult(
    player: Player,
    contents: List[ItemStack],
    tradeResult: TradeResult[ItemStack, TransactionInfo]
  ): F[Unit]

  /**
   * @param contents 取引を行うアイテムのリスト
   * @return 取引を実際に実行し、その結果を返す作用
   */
  final def execute(player: Player, contents: List[ItemStack])(
    implicit tradeRule: TradeRule[ItemStack, TransactionInfo]
  ): F[TradeResult[ItemStack, TransactionInfo]] = {
    for {
      tradeResult <- Sync[F].delay(tradeRule.trade(contents.filterNot(_ == null)))
      _ <- applyTradeResult(player, contents, tradeResult)
    } yield tradeResult
  }

}
