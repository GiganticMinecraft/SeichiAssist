package com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class GachaEvent(eventName: GachaEventName, startDate: LocalDate, endDate: LocalDate) {
  require(startDate.isBefore(endDate))

  /**
   * 今このイベントが開催中かどうか確認する
   * @return 開催中(true) / 開催中じゃない(false)
   */
  def isHolding: Boolean = {
    val now = LocalDate.now()
    val isAfterStartDateOrFirstDay = now.equals(startDate) || now.isAfter(startDate)
    val isBeforeEndDateOrEndDay = now.equals(endDate) || now.isBefore(endDate)

    isAfterStartDateOrFirstDay && isBeforeEndDateOrEndDay
  }

}
