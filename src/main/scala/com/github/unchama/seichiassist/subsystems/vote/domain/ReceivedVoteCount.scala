package com.github.unchama.seichiassist.subsystems.vote.domain
import cats.Order

case class ReceivedVoteCount(value: Int) {
  require(value >= 0, "VoteCountForReceiveは非負である必要があります。")
}

object ReceivedVoteCount {
  implicit val order: Order[ReceivedVoteCount] = Order.by(vc => vc.value)
}
