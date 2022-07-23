package com.github.unchama.seichiassist.subsystems.vote.domain

case class VotePoint(point: Int) extends AnyVal {
  require(point >= 0, "votePointは非負である必要があります。")
}
