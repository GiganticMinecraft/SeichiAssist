package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

import java.time.LocalDate
import java.time.LocalDate.of

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.dateRangeAsSequence

case class LimitedLoginPeriods(startDate: LocalDate, endDate: LocalDate)

object LimitedLoginPeriods {
  private val EVENT_PERIODS = Set(
    LimitedLoginPeriods(of(2019, 3, 1), of(2019, 3, 31))
  )

  def isContainedAt: Option[LimitedLoginPeriods] = EVENT_PERIODS.find {period =>
    dateRangeAsSequence(period.startDate, period.endDate).contains(LocalDate.now())
  }

  def isInEvent: Boolean = isContainedAt.isDefined
}
