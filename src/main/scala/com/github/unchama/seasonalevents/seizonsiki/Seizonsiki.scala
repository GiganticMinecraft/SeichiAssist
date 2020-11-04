package com.github.unchama.seasonalevents.seizonsiki

import java.time.LocalDate

import com.github.unchama.seasonalevents.Util.{validateItemDropRate, validateUrl}

object Seizonsiki {
  def isInEvent: Boolean = LocalDate.now().isBefore(END_DATE)

  // イベントが実際に終了する日。
  val END_DATE: LocalDate = LocalDate.of(2017, 1, 22)
  val itemDropRate: Double = validateItemDropRate(30)
  val blogArticleUrl: String = validateUrl("https://www.seichi.network/blog/categories/%E3%82%A4%E3%83%99%E3%83%B3%E3%83%88%E6%83%85%E5%A0%B1")
}