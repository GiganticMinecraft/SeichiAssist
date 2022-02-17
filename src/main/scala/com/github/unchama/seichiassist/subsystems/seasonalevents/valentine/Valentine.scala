package com.github.unchama.seichiassist.subsystems.seasonalevents.valentine

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{validateItemDropRate, validateUrl}
import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.DateDuration

import java.time.{LocalDate, LocalDateTime}

object Valentine {
  val EVENT_YEAR: Int = 2022

  private val START_DATE = LocalDate.of(EVENT_YEAR, 2, 17)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 2, 27)
  private val duration = DateDuration(START_DATE, END_DATE).asDateTimeDuration()

  val START_DATETIME: LocalDateTime = duration.from
  val END_DATETIME: LocalDateTime = duration.to

  val itemDropRate: Double = validateItemDropRate(0.3)
  val blogArticleUrl: String = validateUrl(s"https://www.seichi.network/post/valentine$EVENT_YEAR")

  def isInEvent: Boolean = duration.isInDuration(LocalDateTime.now())
}
