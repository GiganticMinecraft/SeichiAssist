package com.github.unchama.seichiassist.subsystems.dragonnighttime.application

import com.github.unchama.seichiassist.subsystems.dragonnighttime.domain.{
  DragonNightTime,
  Period
}

import java.time.LocalTime

object DragonNightTimeImpl extends DragonNightTime {
  override protected val effectivePeriodOnWeekdays: Period =
    Period(LocalTime.of(20, 0, 0), LocalTime.of(21, 0, 0))

  override protected val effectivePeriodOnWeekends: Period =
    Period(LocalTime.of(19, 0, 0), LocalTime.of(21, 0, 0))
}
