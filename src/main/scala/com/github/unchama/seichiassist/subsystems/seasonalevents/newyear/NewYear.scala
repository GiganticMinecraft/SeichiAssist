package com.github.unchama.seichiassist.subsystems.seasonalevents.newyear

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{dateRangeAsSequence, validateItemDropRate, validateUrl}
import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.DateTimeDuration

import java.time.{LocalDate, LocalDateTime}

object NewYear {
  // 新年が何年かを西暦で入力しておくと、自動的に他の日付が設定される
  val EVENT_YEAR: Int = 2020
  val START_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 1, 1)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 1, 31)
  // 年越しそばが配布されるのは、大晦日の4:10から元旦の4:10まで
  val NEW_YEAR_EVE: DateTimeDuration = DateTimeDuration.fromLocalDate(START_DATE.minusDays(1), START_DATE)
  val itemDropRate: Double = validateItemDropRate(0.002)
  val blogArticleUrl: String = validateUrl(s"https://www.seichi.network/post/newyear$EVENT_YEAR")

  def sobaWillBeDistributed: Boolean = NEW_YEAR_EVE.isInDuration(LocalDateTime.now())

  def isInEvent: Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(LocalDate.now())
}
