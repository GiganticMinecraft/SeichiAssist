package com.github.unchama.seichiassist.subsystems.dragonnighttime.application

import com.github.unchama.seichiassist.subsystems.dragonnighttime.domain.Period
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.{LocalDate, LocalTime}
import com.github.unchama.seichiassist.subsystems.dragonnighttime.domain.DragonNightTime

class DragonNightTimeImplSpec extends AnyWordSpec with Matchers {
  val weekdayPeriod: Period = Period(LocalTime.of(20, 0, 0), LocalTime.of(21, 0, 0))
  val weekendPeriod: Period = Period(LocalTime.of(19, 0, 0), LocalTime.of(21, 0, 0))

  // 各曜日の代表的な日付
  // 2026-03-16 (月) 〜 2026-03-22 (日)
  val monday: LocalDate = LocalDate.of(2026, 3, 16)
  val tuesday: LocalDate = LocalDate.of(2026, 3, 17)
  val wednesday: LocalDate = LocalDate.of(2026, 3, 18)
  val thursday: LocalDate = LocalDate.of(2026, 3, 19)
  val friday: LocalDate = LocalDate.of(2026, 3, 20)
  val saturday: LocalDate = LocalDate.of(2026, 3, 21)
  val sunday: LocalDate = LocalDate.of(2026, 3, 22)

  val dragonNightTime: DragonNightTime = DragonNightTimeImpl

  "DragonNightTimeImpl" should {
    "月〜金の全曜日は平日ピリオドを返す" in {
      val weekdays = Set(monday, tuesday, wednesday, thursday, friday)
      weekdays.foreach { date =>
        dragonNightTime.effectivePeriod(date) shouldBe weekdayPeriod
      }
    }

    "土・日は週末ピリオドを返す" in {
      val weekends = Set(saturday, sunday)
      weekends.foreach { date => dragonNightTime.effectivePeriod(date) shouldBe weekendPeriod }
    }
  }
}
