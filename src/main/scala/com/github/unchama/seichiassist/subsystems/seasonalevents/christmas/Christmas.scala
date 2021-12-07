package com.github.unchama.seichiassist.subsystems.seasonalevents.christmas

import java.time.LocalDate

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{dateRangeAsSequence, validateItemDropRate, validateUrl}

object Christmas {
  val itemDropRate: Double = validateItemDropRate(0.006)
  val itemDropRateFromStray: Double = validateItemDropRate(0.3)
  val EVENT_YEAR: Int = 2021
  val START_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 12, 15)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 12, 31)
  val blogArticleUrl: String = validateUrl(s"https://www.seichi.network/post/christmas$EVENT_YEAR")

  def isInEvent(date: LocalDate): Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(date)

  // side-effectful
  def isInEventNow: Boolean = isInEvent(LocalDate.now())
}
