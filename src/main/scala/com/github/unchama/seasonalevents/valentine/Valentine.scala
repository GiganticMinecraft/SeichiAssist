package com.github.unchama.seasonalevents.valentine

import java.time.LocalDate

import com.github.unchama.seasonalevents.Util.{validateItemDropRate, validateUrl}

object Valentine {
  def isInEvent: Boolean = LocalDate.now().isBefore(END_DATE)

  // イベントが実際に終了する日
  val END_DATE: LocalDate = LocalDate.of(2018, 2, 27)
  val itemDropRate: Double = validateItemDropRate(30)
  val blogArticleUrl: String = validateUrl("https://www.seichi.network/post/valentine2020")
}