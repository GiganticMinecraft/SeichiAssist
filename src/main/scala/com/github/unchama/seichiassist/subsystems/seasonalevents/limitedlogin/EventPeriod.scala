package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

import java.time.LocalDate

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.dateRangeAsSequence

case class EventPeriod(startDate: LocalDate, endDate: LocalDate)

trait LimitedLoginPeriod {
  val period: EventPeriod

  def isInEvent: Boolean = dateRangeAsSequence(period.startDate, period.endDate).contains(LocalDate.now())
}
