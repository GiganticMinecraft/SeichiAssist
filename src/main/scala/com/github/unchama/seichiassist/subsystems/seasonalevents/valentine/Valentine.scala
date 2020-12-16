package com.github.unchama.seichiassist.subsystems.seasonalevents.valentine

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{dateRangeAsSequence, validateItemDropRate, validateUrl}
import java.time.LocalDate

object Valentine {
  val itemDropRate: Double = validateItemDropRate(0.3)
  val blogArticleUrl: String = validateUrl("https://www.seichi.network/post/valentine2020")
  val EVENT_YEAR: Int = 2021
  val START_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 2, 13)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 2, 27)

  def isInEvent: Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(LocalDate.now())
}