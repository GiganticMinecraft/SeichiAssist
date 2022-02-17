package com.github.unchama.seichiassist.subsystems.seasonalevents.domain

import java.time.LocalDateTime

case class DateTimeDuration(from: LocalDateTime, to: LocalDateTime) {
  require(from.isBefore(to) || from.isEqual(to), "適切ではない期間が指定されました。")

  def isInDuration(base: LocalDateTime): Boolean = {
    val isAfterFrom = base.isEqual(from) || base.isAfter(from)
    val isBeforeTo = base.isEqual(to) || base.isBefore(to)

    isAfterFrom && isBeforeTo
  }
}
