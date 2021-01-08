package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.dateRangeAsSequence

import java.time.LocalDate

case class EventPeriod(startDate: LocalDate, endDate: LocalDate) {
  require(startDate.isBefore(endDate), "'startDate' must be before 'endDate'")
}

trait LimitedLoginPeriod {
  val period: EventPeriod

  final def isInEvent(date: LocalDate): Boolean = dateRangeAsSequence(period.startDate, period.endDate).contains(date)
}
