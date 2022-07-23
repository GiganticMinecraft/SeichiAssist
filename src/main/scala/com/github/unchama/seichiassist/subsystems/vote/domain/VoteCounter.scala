package com.github.unchama.seichiassist.subsystems.vote.domain

case class VoteCounter(value: Int) extends AnyVal {
  require(value >= 0, "votePointは非負である必要があります。")
}
