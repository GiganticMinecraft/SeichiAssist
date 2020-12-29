package com.github.unchama.seichiassist.subsystems.seasonalevents.seizonsiki

import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.{validateItemDropRate, validateUrl}

import java.time.LocalDate

object Seizonsiki {
  val END_DATE: LocalDate = LocalDate.of(2017, 1, 22)
  val itemDropRate: Double = validateItemDropRate(0.3)
  val blogArticleUrl: String = validateUrl(s"https://www.seichi.network/post/seizonsiki${END_DATE.getYear}")

  def isInEvent: Boolean = LocalDate.now().isBefore(END_DATE)
}