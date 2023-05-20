package com.github.unchama.util.time

import java.time.Instant
import ajd4jp.{AJD, LunisolarYear}
import cats.Functor
import cats.effect.Clock
import cats.implicits._

object LunisolarDate {

  /**
   * 現在日時から旧暦の日付を返します。
   */
  def now[F[_]: Clock: Functor]: F[LunisolarDate] = Clock[F].instantNow.map(of)

  /**
   * 指定したInstantから旧暦の日付を返します。
   */
  def of(instant: Instant): LunisolarDate = {
    val ajd = new AJD(instant)
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
