package com.github.unchama.seasonalevents.seizonsiki

import java.time.LocalDate

import com.github.unchama.seasonalevents.SeasonalEventsConfig
import com.github.unchama.seasonalevents.Util.localDateFromYearMonthDays
import com.github.unchama.seichiassist.SeichiAssist

class Seizonsiki(private val plugin: SeichiAssist)(implicit config: SeasonalEventsConfig) {
  private val today = LocalDate.now()
  // イベント開催中か判定
  if (today.isBefore(Seizonsiki.END_DATE)) {
    Seizonsiki.isInEvent = true
    plugin.getServer.getPluginManager.registerEvents(new SeizonsikiListener(), plugin)
  }
}

object Seizonsiki {
  var isInEvent = false

  // イベントが実際に終了する日。
  val END_DATE: LocalDate = localDateFromYearMonthDays(2017, 1, 22)
  // 表記上の終了日。サーバーは午前4時に再起動するため、開催中の判定が行われるのはその時。
  val DISPLAYED_END_DATE: LocalDate = END_DATE.minusDays(1)
}