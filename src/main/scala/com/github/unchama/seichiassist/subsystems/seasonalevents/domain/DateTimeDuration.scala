package com.github.unchama.seichiassist.subsystems.seasonalevents.domain

import java.time.{LocalDate, LocalDateTime, LocalTime}

case class DateTimeDuration(from: LocalDateTime, to: LocalDateTime) {
  require(from.isBefore(to) || from.isEqual(to), "適切ではない期間が指定されました。")

  private val REBOOT_TIME = LocalTime.of(4, 10)

  def this(from: LocalDate, to: LocalDate) = this(LocalDateTime.of(from, REBOOT_TIME), LocalDateTime.of(to, REBOOT_TIME))

  def isInDuration(base: LocalDateTime): Boolean = {
    val isAfterFrom = base.isEqual(from) || base.isAfter(from)
    val isBeforeTo = base.isEqual(to) || base.isBefore(to)

    isAfterFrom && isBeforeTo
  }
}
