package com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{
  dateRangeAsSequence,
  validateUrl
}

import java.time.LocalDate

object Anniversary {
  val EVENT_YEAR = 2022
  val ANNIVERSARY_COUNT: Int = EVENT_YEAR - 2016
  val START_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 6, 29)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 7, 5)
  val blogArticleUrl: String = validateUrl(
    s"https://www.seichi.network/post/${ANNIVERSARY_COUNT}th_anniv"
  )

  def isInEvent: Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(LocalDate.now())
}
