package com.github.unchama.seichiassist.subsystems.seasonalevents.halloween

import java.time.LocalDate

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{dateRangeAsSequence, validateUrl}

object Halloween {
  val blogArticleUrl: String = validateUrl("https://www.seichi.network/post/halloween2020")
  val EVENT_YEAR: Int = 2020
  val START_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 10, 15)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 10, 31)

  def isInEvent: Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(LocalDate.now())
}