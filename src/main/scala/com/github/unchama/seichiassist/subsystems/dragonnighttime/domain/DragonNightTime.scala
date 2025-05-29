package com.github.unchama.seichiassist.subsystems.dragonnighttime.domain

import java.time.{DayOfWeek, LocalDate}

trait DragonNightTime {
  val effectivePeriodOnWeekdays: Period
  val effectivePeriodOnWeekends: Period

  final def effectivePeriod(today: LocalDate): Period = {
    val todayWeekday = today.getDayOfWeek

    if (todayWeekday == DayOfWeek.SATURDAY || todayWeekday == DayOfWeek.SUNDAY)
      effectivePeriodOnWeekends
    else effectivePeriodOnWeekdays
  }
}
