package com.github.unchama.seichiassist.subsystems.gacha.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._

class GachaDrawSettings[F[_]: Sync] {

  val consumeGachaTicketAmountReference: F[Ref[F, ConsumeAmount]] =
    Ref.of(ConsumeAmount.oneThousand)
  import ConsumeAmount._
  private val toggleConsumeGachaTicketAmountOrder = Map[ConsumeAmount, ConsumeAmount](
    oneThousand -> fiveThousands,
    fiveThousands -> tenThousands,
    tenThousands -> oneThousand
  )
  def toggleConsumeGachaTicketAmount(): F[Unit] =
    for {
      ref <- consumeGachaTicketAmountReference
      _ <- ref.update(oldValue => toggleConsumeGachaTicketAmountOrder(oldValue))
    } yield ()

  def consumeGachaTicketAmount(): F[ConsumeAmount] =
    for {
      ref <- consumeGachaTicketAmountReference
      value <- ref.get
    } yield value
}
