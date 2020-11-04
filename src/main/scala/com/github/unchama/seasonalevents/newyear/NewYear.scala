package com.github.unchama.seasonalevents.newyear

import java.time.LocalDate

import com.github.unchama.seasonalevents.Util.getDateSeq

object NewYear {
  // 新年が何年かを西暦で入力しておくと、自動的に他の日付が設定される
  val EVENT_YEAR = 2018
  val START_DATE: LocalDate = LocalDate.ofYearDay(EVENT_YEAR, 1)
  val END_DATE: LocalDate = LocalDate.ofYearDay(EVENT_YEAR, 31)
  val DISTRIBUTED_SOBA_DATE: LocalDate = LocalDate.ofYearDay(EVENT_YEAR - 1, 365)

  // お年玉袋ドロップ数(1/nブロック)
  val itemDropRate = 500

  def sobaWillBeDistributed: Boolean = LocalDate.now().isEqual(DISTRIBUTED_SOBA_DATE)

  def newYearBagWillBeDropped: Boolean = getDateSeq(START_DATE, END_DATE).contains(LocalDate.now())
}