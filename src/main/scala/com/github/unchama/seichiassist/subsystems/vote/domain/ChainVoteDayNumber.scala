package com.github.unchama.seichiassist.subsystems.vote.domain

case class ChainVoteDayNumber(value: Int) extends AnyVal {
  require(value >= 0, "投票日数は非負である必要があります。")
}
