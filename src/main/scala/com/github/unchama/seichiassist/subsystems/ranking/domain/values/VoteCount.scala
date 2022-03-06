package com.github.unchama.seichiassist.subsystems.ranking.domain.values

import cats.Order
import cats.kernel.Monoid

/**
 * 投票数。 TODO: rankingサブシステムから移す
 */
case class VoteCount(value: Int) extends AnyVal

object VoteCount {
  implicit val order: Order[VoteCount] = Order.by(_.value)
  implicit val monoid: Monoid[VoteCount] =
    Monoid.instance(VoteCount(0), (a, b) => VoteCount(a.value + b.value))
}
