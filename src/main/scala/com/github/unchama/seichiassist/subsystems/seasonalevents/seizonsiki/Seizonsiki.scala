package com.github.unchama.seichiassist.subsystems.seasonalevents.seizonsiki

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{validateItemDropRate, validateUrl}

import java.time.LocalDate

object Seizonsiki {
  def isInEvent: Boolean = LocalDate.now().isBefore(END_DATE)

  // イベントが実際に終了する日。
  val END_DATE: LocalDate = LocalDate.of(2017, 1, 22)
  val itemDropRate: Double = validateItemDropRate(0.3)
  val blogArticleUrl: String = validateUrl("https://www.seichi.network/post/seizonsiki2020")
}