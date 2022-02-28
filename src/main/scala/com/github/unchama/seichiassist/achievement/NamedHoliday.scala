package com.github.unchama.seichiassist.achievement

import enumeratum._

import java.time.{LocalDate, Month}
import scala.math.floor

/**
 * 年によって日付が変わってしまうので、各年ごとに計算が必要な日の列挙
 */
sealed abstract class NamedHoliday(val name: String) extends EnumEntry {
  def dateOn(year: Int): LocalDate
}

case object NamedHoliday extends Enum[NamedHoliday] {
  val values: IndexedSeq[NamedHoliday] = findValues

  case object SpringEquinoxDay extends NamedHoliday("春分の日") {
    override def dateOn(year: Int): LocalDate = {
      val dayOfMonth = floor(20.8431 + 0.242194 * (year - 1980) - (year - 1980) / 4).toInt

      LocalDate.of(year, Month.MARCH, dayOfMonth)
    }
  }
}
