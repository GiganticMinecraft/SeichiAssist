package com.github.unchama.seasonalevents.valentine

import java.time.LocalDate

import com.github.unchama.seasonalevents.Util.localDateFromYearMonthDays
import org.bukkit.plugin.Plugin

class Valentine(private val plugin: Plugin) {
  // イベント開催中か判定
  private val today = LocalDate.now()
  if (today.isBefore(Valentine.END_DATE)) {
    plugin.getServer.getPluginManager.registerEvents(new ValentineListener(), plugin)
    Valentine.isInEvent = true
  }
  if (today.isBefore(Valentine.DROP_END_DATE)) Valentine.itemsWillBeDropped = true
}

object Valentine {
  var itemsWillBeDropped = false
  // SeichiAssistで呼ばれてるだけ
  var isInEvent: Boolean = false

  // イベントが実際に終了する日
  val END_DATE: LocalDate = localDateFromYearMonthDays(2018, 2, 27)
  // ドロップが実際に終了する日
  val DROP_END_DATE: LocalDate = localDateFromYearMonthDays(2018, 2, 20)
  val DISPLAYED_END_DATE: LocalDate = END_DATE.minusDays(1)
  val DISPLAYED_DROP_END_DATE: LocalDate = DROP_END_DATE.minusDays(1)
}