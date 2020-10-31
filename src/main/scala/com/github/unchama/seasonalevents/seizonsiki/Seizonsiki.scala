package com.github.unchama.seasonalevents.seizonsiki

import java.time.LocalDate

import com.github.unchama.seasonalevents.Util.localDateFromYearMonthDays
import org.bukkit.plugin.Plugin

class Seizonsiki(private val plugin: Plugin) {
  private val today = LocalDate.now()
  // イベント開催中か判定
  if (today.isBefore(Seizonsiki.END_DATE)) plugin.getServer.getPluginManager.registerEvents(new SeizonsikiListener(), plugin)
  if (today.isBefore(Seizonsiki.DROP_END_DATE)) Seizonsiki.isDrop = true
}

object Seizonsiki {
  var isDrop = false

  // イベントが実際に終了する日
  val END_DATE: LocalDate = localDateFromYearMonthDays(2017, 1, 22)
  // ドロップが実際に終了する日
  val DROP_END_DATE: LocalDate = localDateFromYearMonthDays(2017, 1, 16)
  val DISPLAYED_END_DATE: LocalDate = END_DATE.minusDays(1)
  val DISPLAYED_DROP_END_DATE: LocalDate = DROP_END_DATE.minusDays(1)
}