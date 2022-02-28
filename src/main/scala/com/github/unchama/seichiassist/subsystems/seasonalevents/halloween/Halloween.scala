package com.github.unchama.seichiassist.subsystems.seasonalevents.halloween

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{
  dateRangeAsSequence,
  validateUrl
}

import java.time.LocalDate

object Halloween {
  val EVENT_YEAR: Int = 2021
  val START_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 10, 18)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 11, 15)
  val blogArticleUrl: String = validateUrl(
    s"https://www.seichi.network/post/halloween$EVENT_YEAR"
  )

  def isInEvent: Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(LocalDate.now())
}
