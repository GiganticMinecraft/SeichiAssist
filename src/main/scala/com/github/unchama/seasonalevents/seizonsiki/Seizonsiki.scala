package com.github.unchama.seasonalevents.seizonsiki

import java.time.LocalDate

import com.github.unchama.seasonalevents.Util.localDateFromYearMonthDays

object Seizonsiki {
  def isInEvent: Boolean = LocalDate.now().isBefore(END_DATE)

  // イベントが実際に終了する日。
  val END_DATE: LocalDate = localDateFromYearMonthDays(2017, 1, 22)
}