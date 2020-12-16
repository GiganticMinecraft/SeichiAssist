package com.github.unchama.seichiassist.subsystems.seasonalevents.newyear

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{dateRangeAsSequence, validateItemDropRate, validateUrl}
import java.time.LocalDate

object NewYear {
  val itemDropRate: Double = validateItemDropRate(0.002)
  val blogArticleUrl: String = validateUrl("https://www.seichi.network/post/newyear2021")
  // 新年が何年かを西暦で入力しておくと、自動的に他の日付が設定される
  val EVENT_YEAR: Int = 2021
  val START_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 1, 1)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 1, 31)
  val DISTRIBUTED_SOBA_DATE: LocalDate = START_DATE.minusDays(1)

  def sobaWillBeDistributed: Boolean = LocalDate.now().isEqual(DISTRIBUTED_SOBA_DATE)

  def isInEvent: Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(LocalDate.now())
}