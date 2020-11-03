package com.github.unchama.seasonalevents.valentine

import java.time.LocalDate

import com.github.unchama.seasonalevents.Util.localDateFromYearMonthDays

object Valentine {
  def isInEvent: Boolean = LocalDate.now().isBefore(END_DATE)

  // イベントが実際に終了する日
  val END_DATE: LocalDate = localDateFromYearMonthDays(2018, 2, 27)
}