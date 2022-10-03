package com.github.unchama.seichiassist.subsystems.gacha.domain.gachaevent

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class GachaEvent(eventName: GachaEventName, startDate: LocalDate, endDate: LocalDate) {
  require(startDate.isBefore(endDate))

  private def toTimeString(localDate: LocalDate): String = {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    dateTimeFormatter.format(localDate)
  }

  def getStartDateString: String = toTimeString(startDate)

  def getEndDateString: String = toTimeString(endDate)

}
