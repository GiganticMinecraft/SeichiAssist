package com.github.unchama.seichiassist.subsystems.vote.domain

case class ChainVoteDayNumber(dayNumber: Int) extends AnyVal {
  require(dayNumber >= 0, "投票日数は非負である必要があります。")
}
