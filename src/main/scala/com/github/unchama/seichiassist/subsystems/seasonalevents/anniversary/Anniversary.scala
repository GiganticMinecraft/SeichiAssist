package com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.validateUrl

import java.time.LocalDate

object Anniversary {
  val EVENT_YEAR = 2021
  val ANNIVERSARY_COUNT = EVENT_YEAR - 2016
  val EVENT_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 6, 29)
  val blogArticleUrl: String = validateUrl(s"https://www.seichi.network/post/${ANNIVERSARY_COUNT}th_anniv")

  def isInEvent: Boolean = EVENT_DATE.equals(LocalDate.now())
}
