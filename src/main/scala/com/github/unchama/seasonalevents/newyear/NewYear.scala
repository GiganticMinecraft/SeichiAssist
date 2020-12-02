package com.github.unchama.seasonalevents.newyear

import java.time.LocalDate

import com.github.unchama.seasonalevents.Util.{dateRangeAsSequence, validateItemDropRate}

object NewYear {
  val itemDropRate: Double = validateItemDropRate(0.002)
  // 新年が何年かを西暦で入力しておくと、自動的に他の日付が設定される
  val EVENT_YEAR: Int = 2018
  val START_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 1, 1)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 1, 31)
  val DISTRIBUTED_SOBA_DATE: LocalDate = START_DATE.minusDays(1)

  def sobaWillBeDistributed: Boolean = LocalDate.now().isEqual(DISTRIBUTED_SOBA_DATE)

  def isInEvent: Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(LocalDate.now())
}