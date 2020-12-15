package com.github.unchama.seichiassist.subsystems.seasonalevents.christmas

import java.time.LocalDate

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{dateRangeAsSequence, validateItemDropRate, validateUrl}

object Christmas {
  val itemDropRate: Double = validateItemDropRate(0.002)
  val itemDropRateFromStray: Double = validateItemDropRate(0.1)
  val blogArticleUrl: String = validateUrl("https://www.seichi.network/post/christmas2020")
  val EVENT_YEAR: Int = 2020
  val START_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 12, 15)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 12, 29)

  def isInEvent(date: LocalDate): Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(date)

  // side-effectful
  def isInEventNow: Boolean = isInEvent(LocalDate.now())
}