package com.github.unchama.seichiassist.subsystems.seasonalevents.christmas

import java.time.LocalDate

object Christmas {
  val itemDropRate: Double = validateItemDropRate(0.002)
  val blogArticleUrl: String = validateUrl("https://www.seichi.network/post/christmas2020")
  val EVENT_YEAR: Int = 2020
  val START_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 12, 15)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 12, 29)

  def isInEvent: Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(LocalDate.now())
}