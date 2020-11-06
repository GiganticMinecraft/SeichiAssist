package com.github.unchama.seasonalevents.anniversary

import java.time.LocalDate

import com.github.unchama.seasonalevents.Util.validateUrl

object Anniversary {
  val ANNIVERSARY_COUNT = EVENT_YEAR - 2016
  val EVENT_YEAR = 2020
  val EVENT_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 6, 29)
  val blogArticleUrl: String = validateUrl("https://www.seichi.network/blog/categories/%E3%82%A4%E3%83%99%E3%83%B3%E3%83%88%E6%83%85%E5%A0%B1")
}