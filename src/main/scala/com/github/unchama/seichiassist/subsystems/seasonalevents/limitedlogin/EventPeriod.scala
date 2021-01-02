package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

import java.time.LocalDate

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.dateRangeAsSequence

case class EventPeriod(startDate: LocalDate, endDate: LocalDate)

trait LimitedLoginPeriod {
  val EVENT_PERIOD: EventPeriod

  def isInEvent: Boolean = dateRangeAsSequence(EVENT_PERIOD.startDate, EVENT_PERIOD.endDate).contains(LocalDate.now())
}
