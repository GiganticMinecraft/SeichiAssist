package com.github.unchama.seasonalevents.christmas

import java.time.LocalDate

import com.github.unchama.seasonalevents.Util.{dateRangeAsSequence, validateItemDropRate, validateUrl}

object Christmas {
  val itemDropRate: Double = validateItemDropRate(0.002)
  val blogArticleUrl: String = validateUrl("https://www.seichi.network/post/christmas2020")
  val EVENT_YEAR: Int = 2020
  val START_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 12, 15)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 12, 26)

  def isInEvent: Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(LocalDate.now())
}