package com.github.unchama.seichiassist.subsystems.seasonalevents.valentine

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{
  validateItemDropRate,
  validateUrl
}
import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.DateTimeDuration

import java.time.{LocalDate, LocalDateTime}

object Valentine {
  val EVENT_YEAR: Int = 2022

  private val START_DATE = LocalDate.of(EVENT_YEAR, 2, 17)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 2, 27)
  val EVENT_DURATION: DateTimeDuration = DateTimeDuration.fromLocalDate(START_DATE, END_DATE)
  val itemDropRate: Double = validateItemDropRate(0.3)
  val blogArticleUrl: String = validateUrl(
    s"https://www.seichi.network/post/valentine$EVENT_YEAR"
  )

  def isInEvent: Boolean = EVENT_DURATION.contains(LocalDateTime.now())
}
