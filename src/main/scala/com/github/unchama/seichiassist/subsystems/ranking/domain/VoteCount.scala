package com.github.unchama.seichiassist.subsystems.ranking.domain

import cats.Order

/**
 * 投票数。 TODO: rankingサブシステムから移す
 */
case class VoteCount(value: Int) extends AnyVal

object VoteCount {
  implicit val order: Order[VoteCount] = Order.by(_.value)
}
