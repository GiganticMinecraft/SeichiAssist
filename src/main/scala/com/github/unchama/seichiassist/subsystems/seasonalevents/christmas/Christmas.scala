package com.github.unchama.seichiassist.subsystems.seasonalevents.christmas

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{
  dateRangeAsSequence,
  validateItemDropRate,
  validateUrl
}

import java.time.LocalDate

object Christmas {
  // FIXME: 2022年は2021年の補填も含めて2倍の排出率になっているので2023年以降に開催する場合は戻す
  val itemDropRate: Double = validateItemDropRate(0.012)
  val itemDropRateFromStray: Double = validateItemDropRate(0.6)
  val EVENT_YEAR: Int = 2022
  val START_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 12, 19)
  val END_DATE: LocalDate = LocalDate.of(EVENT_YEAR, 12, 31)
  val blogArticleUrl: String = validateUrl(
    s"https://www.seichi.network/post/christmas$EVENT_YEAR"
  )

  def isInEvent(date: LocalDate): Boolean =
    dateRangeAsSequence(START_DATE, END_DATE).contains(date)

  // side-effectful
  def isInEventNow: Boolean = isInEvent(LocalDate.now())
}
