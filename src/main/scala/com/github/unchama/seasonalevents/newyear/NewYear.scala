package com.github.unchama.seasonalevents.newyear

import java.time.LocalDate

import com.github.unchama.seasonalevents.Util.{dateRangeAsSequence, validateItemDropRate}

object NewYear {
  // お年玉袋ドロップ率（%）
  // 旧実装：ドロップ数 1/nブロック、初期値 n=500
  val itemDropRate: Double = validateItemDropRate(0.2)
  // 新年が何年かを西暦で入力しておくと、自動的に他の日付が設定される
  val EVENT_YEAR: Int = 2018
  val PREV_EVENT_YEAR: Int = EVENT_YEAR - 1
  val START_DATE: LocalDate = LocalDate.ofYearDay(EVENT_YEAR, 1)
  val END_DATE: LocalDate = LocalDate.ofYearDay(EVENT_YEAR, 31)
  val DISTRIBUTED_SOBA_DATE: LocalDate = LocalDate.ofYearDay(PREV_EVENT_YEAR, 365)

  def sobaWillBeDistributed: Boolean = LocalDate.now().isEqual(DISTRIBUTED_SOBA_DATE)

  def isInEvent: Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(LocalDate.now())
}