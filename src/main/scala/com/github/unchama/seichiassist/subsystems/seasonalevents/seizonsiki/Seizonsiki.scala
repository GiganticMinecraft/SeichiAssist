package com.github.unchama.seichiassist.subsystems.seasonalevents.seizonsiki

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{
  dateRangeAsSequence,
  validateItemDropRate,
  validateUrl
}

import java.time.LocalDate

object Seizonsiki {
  val EVENT_YEAR: Int = 202
  val START_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 1, 9)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 1, 22)
  val itemDropRate: Double = validateItemDropRate(0.3)
  // 2022年は新年イベントのブログ記事と同じ記事に記載
  val blogArticleUrl: String = validateUrl(
    s"https://www.seichi.network/post/newyear$EVENT_YEAR"
  )

  def isInEvent: Boolean = dateRangeAsSequence(START_DATE, END_DATE).contains(LocalDate.now())
}
