package com.github.unchama.seichiassist.subsystems.gacha.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._

class GachaDrawSettings[F[_]: Sync] {

  import GachaTicketConsumeAmount._

  private val consumeGachaTicketAmountReference: Ref[F, GachaTicketConsumeAmount] =
    Ref.unsafe(oneThousand)

  private val toggleConsumeGachaTicketAmountOrder = Map[GachaTicketConsumeAmount, GachaTicketConsumeAmount](
    oneThousand -> fiveThousands,
    fiveThousands -> tenThousands,
    tenThousands -> oneThousand
  )

  def toggleConsumeGachaTicketAmount(): F[Unit] = {
    for {
      _ <- consumeGachaTicketAmountReference.update(oldValue =>
        toggleConsumeGachaTicketAmountOrder(oldValue)
      )
    } yield ()
  }
  
  def consumeGachaTicketAmount(): F[GachaTicketConsumeAmount] =
    for {
      value <- consumeGachaTicketAmountReference.get
    } yield value
}
