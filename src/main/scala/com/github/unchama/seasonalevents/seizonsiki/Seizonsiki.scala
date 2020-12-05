package com.github.unchama.seasonalevents.seizonsiki

import java.time.LocalDate

import com.github.unchama.seasonalevents.Util.{validateItemDropRate, validateUrl}

object Seizonsiki {
  def isInEvent: Boolean = LocalDate.now().isBefore(END_DATE)

  // イベントが実際に終了する日。
  val END_DATE: LocalDate = LocalDate.of(2017, 1, 22)
  val itemDropRate: Double = validateItemDropRate(0.3)
  val blogArticleUrl: String = validateUrl("https://www.seichi.network/post/seizonsiki2020")
}