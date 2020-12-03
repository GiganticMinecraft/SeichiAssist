package com.github.unchama.seasonalevents.limitedlogin

import java.time.LocalDate

import com.github.unchama.seasonalevents.Util.dateRangeAsSequence

object LimitedLoginEvent {
  val START_DATE: LocalDate = LocalDate.of(2019, 3, 1)
  val END_DATE: LocalDate = LocalDate.of(2019, 3, 31)

  def isInEvent: Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(LocalDate.now())
}