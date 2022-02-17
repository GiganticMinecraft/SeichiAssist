package com.github.unchama.seichiassist.subsystems.seasonalevents.domain

import java.time.{LocalDate, LocalDateTime, LocalTime}

case class DateDuration(from: LocalDate, to: LocalDate) {
  require(from.isBefore(to) || from.isEqual(to), "適切ではない期間が指定されました。")

  private val REBOOT_TIME = LocalTime.of(4, 10)

  def isInDuration(base: LocalDate): Boolean = {
    val isAfterFrom = base.isEqual(from) || base.isAfter(from)
    val isBeforeTo = base.isEqual(to) || base.isBefore(to)

    isAfterFrom && isBeforeTo
  }

  def asDateTimeDuration(): DateTimeDuration =
    DateTimeDuration(LocalDateTime.of(from, REBOOT_TIME), LocalDateTime.of(to, REBOOT_TIME))
}
