package com.github.unchama.seichiassist.subsystems.vote.domain

case class VoteCounter(value: Int) {
  require(value >= 0, "VoteCounterは非負である必要があります。")
}
