package com.github.unchama.util.time

import java.time.{LocalDateTime, ZoneId}
import ajd4jp.{AJD, LunisolarYear}

object LunisolarDate {

  /**
   * 現在日時から旧暦の日付を返します。
   */
  def now(): LunisolarDate = of(LocalDateTime.now())

  /**
   * 指定したLocalDateTimeから旧暦の日付を返します。タイムゾーンは実行マシン依存です。
   */
  def of(t: LocalDateTime): LunisolarDate = {
    val ajd = new AJD(t.toInstant(ZoneId.systemDefault.getRules.getOffset(t)))
    val lunisolarDate = LunisolarYear.getLunisolarYear(ajd).getLSCD(ajd)
    LunisolarDate(
      lunisolarDate.getYear,
      lunisolarDate.getMonth,
      lunisolarDate.isLeapMonth,
      lunisolarDate.getDay
    )
  }

}

/**
 * 旧暦（太陰太陽暦）の日付を表すクラス
 * @param year 年
 * @param month 月
 * @param isLeapMonth 閏月かどうか
 * @param dayOfMonth 日
 */
case class LunisolarDate(year: Int, month: Int, isLeapMonth: Boolean, dayOfMonth: Int)
