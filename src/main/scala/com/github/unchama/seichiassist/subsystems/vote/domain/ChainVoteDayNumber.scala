package com.github.unchama.seichiassist.subsystems.vote.domain

case class ChainVoteDayNumber private (dayNumber: Int)

object ChainVoteDayNumber {

  def ofNonNegative(dayNumber: Int): ChainVoteDayNumber = {
    require(dayNumber >= 0, "投票日数は非負である必要があります。")
    ChainVoteDayNumber(dayNumber)
  }

}
