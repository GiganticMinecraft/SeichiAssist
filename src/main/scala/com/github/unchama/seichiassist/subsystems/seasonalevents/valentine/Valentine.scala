package com.github.unchama.seichiassist.subsystems.seasonalevents.valentine

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{dateRangeAsSequence, validateItemDropRate, validateUrl}
import java.time.LocalDate

object Valentine {
  val EVENT_YEAR: Int = 2022
  val START_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 2, 15)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 2, 27)
  val itemDropRate: Double = validateItemDropRate(0.3)
  val blogArticleUrl: String = validateUrl(s"https://www.seichi.network/post/valentine$EVENT_YEAR")

  def isInEvent: Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(LocalDate.now())
}
