package com.github.unchama.seichiassist.subsystems.gacha.subsystems.consumegachaticket.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._

class ConsumeGachaTicketSettings[F[_]: Sync] {

  import GachaTicketConsumeAmount._

  private val consumeGachaTicketAmountReference: Ref[F, GachaTicketConsumeAmount] =
    Ref.unsafe(oneThousand)

  private val toggleConsumeGachaTicketAmountOrder
    : Map[GachaTicketConsumeAmount, GachaTicketConsumeAmount] =
    Map(
      oneThousand -> fiveThousands,
      fiveThousands -> tenThousands,
      tenThousands -> oneThousand
    )

  /**
   * @return プレイヤー毎に保持している一括まとめ引きガチャ券の消費枚数を切り替える作用
   * 設定値が一巡すると元に戻る
   */
  def toggleConsumeGachaTicketAmount(): F[Unit] = {
    for {
      _ <- consumeGachaTicketAmountReference.update { oldValue =>
        toggleConsumeGachaTicketAmountOrder(oldValue)
      }
    } yield ()
  }

  /**
   * @return プレイヤー毎に保持している一括まとめ引きガチャ券の消費枚数を取得する作用
   */
  def consumeGachaTicketAmount(): F[GachaTicketConsumeAmount] =
    for {
      value <- consumeGachaTicketAmountReference.get
    } yield value
}
