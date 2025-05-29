package com.github.unchama.seichiassist.subsystems.dragonnighttime.domain

import java.time.{LocalDate, LocalDateTime}

trait DragonNightTime {
  val effectivePeriodOnWeekdays: Period
  val effectivePeriodOnWeekends: Period

  def effectivePeriod(today: LocalDate): Period
}
