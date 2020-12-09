package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.dateRangeAsSequence

import java.time.LocalDate

object LimitedLoginEvent {
  val START_DATE: LocalDate = LocalDate.of(2019, 3, 1)
  val END_DATE: LocalDate = LocalDate.of(2019, 3, 31)

  def isInEvent: Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(LocalDate.now())
}