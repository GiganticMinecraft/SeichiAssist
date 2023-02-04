package com.github.unchama.seichiassist.subsystems.seasonalevents.valentine

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{
  validateItemDropRate,
  validateUrl
}
import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.DateTimeDuration

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

object Valentine {
  val EVENT_YEAR: Int = 2023

  private val START_DATE = LocalDate.of(EVENT_YEAR, 2, 13)
  private val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 2, 27)
  val EVENT_DURATION: DateTimeDuration = DateTimeDuration.fromLocalDate(START_DATE, END_DATE)
  val END_DATE_TIME: String =
    EVENT_DURATION.to.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
  val itemDropRate: Double = validateItemDropRate(0.3)
  val blogArticleUrl: String = validateUrl(
    s"https://www.seichi.network/post/valentine$EVENT_YEAR"
  )

  def isInEvent: Boolean = EVENT_DURATION.contains(LocalDateTime.now())
}
