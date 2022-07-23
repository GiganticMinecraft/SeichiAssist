package com.github.unchama.seichiassist.subsystems.vote.domain

case class VotePoint private (point: Int)

object VotePoint {

  def ofNonNegative(point: Int): VotePoint = {
    require(point >= 0, "votePointは非負である必要があります。")
    VotePoint(point)
  }

}
