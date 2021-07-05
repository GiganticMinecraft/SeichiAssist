package com.github.unchama.seichiassist.achievement

import java.time.{LocalDate, Month}

import enumeratum._

import scala.math.floor

/**
 * 年によって日付が変わってしまうので、各年ごとに計算が必要な日の列挙
 */
sealed class NamedHoliday(val month: Month, val name: String) extends EnumEntry

case object NamedHoliday extends Enum[NamedHoliday] {
  val values: IndexedSeq[NamedHoliday] = findValues

  case object SpringEquinoxDay extends NamedHoliday(Month.MARCH, "春分の日")

  implicit class NamedHolidayOps(val holiday: NamedHoliday) extends AnyVal {
    def getDayOfMonth(): Int = {
      val year = LocalDate.now().getYear

      floor(holiday match {
        // 春分の日の計算方法：[[http://hp.vector.co.jp/authors/VA006522/zatugaku/syunbun.txt]]
        case SpringEquinoxDay => 20.8431 + 0.242194 * (year - 1980) - (year - 1980) / 4
      }).toInt
    }
  }
}
