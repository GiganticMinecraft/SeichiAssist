package com.github.unchama.seichiassist.subsystems.seasonalevents.domain

import java.time.{LocalDate, LocalDateTime, LocalTime}

case class DateTimeDuration(from: LocalDateTime, to: LocalDateTime) {
  require(from.isBefore(to) || from.isEqual(to), "期間の開始日が終了日よりも後に指定されています。")

  def this(from: LocalDate, to: LocalDate) = this(
    LocalDateTime.of(from, LocalTime.of(4, 10)),
    LocalDateTime.of(to, LocalTime.of(4, 10))
  )

  def isInDuration(base: LocalDateTime): Boolean = {
    val isAfterFrom = base.isEqual(from) || base.isAfter(from)
    val isBeforeTo = base.isEqual(to) || base.isBefore(to)

    isAfterFrom && isBeforeTo
  }
}
