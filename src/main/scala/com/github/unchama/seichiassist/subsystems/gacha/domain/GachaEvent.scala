package com.github.unchama.seichiassist.subsystems.gacha.domain

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class GachaEvent(eventName: String, startTime: LocalDateTime, endTime: LocalDateTime) {
  require(eventName != null && startTime.isBefore(endTime))

  private def toTimeString(localDateTime: LocalDateTime): String = {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:MM:SS")
    dateTimeFormatter.format(localDateTime)
  }

  def getStartTimeString: String = toTimeString(startTime)

  def getEndTimeString: String = toTimeString(endTime)

}
