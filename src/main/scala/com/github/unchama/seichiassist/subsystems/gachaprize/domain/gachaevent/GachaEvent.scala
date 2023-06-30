package com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class GachaEvent(eventName: GachaEventName, startDate: LocalDate, endDate: LocalDate) {
  require(startDate.isBefore(endDate))

  private def toTimeString(localDate: LocalDate): String = {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    dateTimeFormatter.format(localDate)
  }

  /**
   * @return 開始日を文字列にして返す
   */
  def getStartDateString: String = toTimeString(startDate)

  /**
   * @return 終了日を文字列にして返す
   */
  def getEndDateString: String = toTimeString(endDate)

  /**
   * 今このイベントが開催中かどうか確認する
   * @return 開催中(true) / 開催中じゃない(false)
   */
  def isHolding: Boolean = {
    val now = LocalDate.now()
    val isAfterStartDateOrFirstDay = now.equals(startDate) || now.isAfter(startDate)
    val isBeforeEndDateOrFinalDay = now.equals(endDate) || now.isBefore(endDate)

    isAfterStartDateOrFirstDay && isBeforeEndDateOrFinalDay
  }

}
