package com.github.unchama.seasonalevents.valentine

import java.time.LocalDate

import com.github.unchama.seasonalevents.Util.{localDateFromYearMonthDays, validateItemDropRate, validateUrl}

object Valentine {
  def isInEvent: Boolean = LocalDate.now().isBefore(END_DATE)

  // イベントが実際に終了する日
  val END_DATE: LocalDate = localDateFromYearMonthDays(2018, 2, 27)
  val itemDropRate: Double = validateItemDropRate(30)
  val blogArticleUrl: String = validateUrl("https://www.seichi.network/blog/categories/%E3%82%A4%E3%83%99%E3%83%B3%E3%83%88%E6%83%85%E5%A0%B1")
}