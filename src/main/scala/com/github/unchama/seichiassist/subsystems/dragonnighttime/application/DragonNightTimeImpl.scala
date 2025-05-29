package com.github.unchama.seichiassist.subsystems.dragonnighttime.application

import com.github.unchama.seichiassist.subsystems.dragonnighttime.domain.{
  DragonNightTime,
  Period
}

import java.time.{DayOfWeek, LocalDate, LocalTime}

object DragonNightTimeImpl extends DragonNightTime {

  override val effectivePeriodOnWeekdays: Period =
    Period(LocalTime.of(20, 0, 0), LocalTime.of(21, 0, 0))

  override val effectivePeriodOnWeekends: Period =
    Period(LocalTime.of(19, 0, 0), LocalTime.of(21, 0, 0))

  override def effectivePeriod(today: LocalDate): Period = {
    val todayWeekday = today.getDayOfWeek

    if (todayWeekday == DayOfWeek.SATURDAY || todayWeekday == DayOfWeek.SUNDAY)
      effectivePeriodOnWeekends
    else effectivePeriodOnWeekdays
  }
}
