package com.github.unchama.seichiassist.subsystems.vote.domain

case class VoteCount(value: Int) {
  require(value >= 0, "VoteCountは非負である必要があります。")
}
