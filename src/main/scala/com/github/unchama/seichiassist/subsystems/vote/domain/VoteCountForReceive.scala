package com.github.unchama.seichiassist.subsystems.vote.domain
import cats.Order

case class VoteCountForReceive(value: Int) {
  require(value >= 0, "VoteCountForReceiveは非負である必要があります。")
}

object VoteCountForReceive {
  implicit val order: Order[VoteCountForReceive] = Order.by(vc => vc.value)
}
