package com.github.unchama.seasonalevents.limitedlogin

import java.time.LocalDate

import com.github.unchama.seasonalevents.Util.getDateSeq

object LimitedLoginEvent {
  val START_DATE: LocalDate = LocalDate.of(2019, 3, 1)
  val END_DATE: LocalDate = LocalDate.of(2019, 3, 31)
  val EVENT_PERIOD: Seq[LocalDate] = getDateSeq(START_DATE, END_DATE)

  def isInEvent: Boolean = EVENT_PERIOD.contains(LocalDate.now())
}