package com.github.unchama.seichiassist.subsystems.seasonalevents.domain

import org.scalatest.wordspec.AnyWordSpec

import java.time.{LocalDate, LocalDateTime, LocalTime}

class DateTimeDurationSpecUtils extends AnyWordSpec {
  private val rebootTime = LocalTime.of(4, 10)
  private val t = LocalDateTime.of(LocalDate.of(2022, 1, 1), rebootTime)
  private val tPlusOneYear = t.plusYears(1)
  private val dateFrom = t.toLocalDate
  private val dateTo = tPlusOneYear.toLocalDate

  "DateTimeDuration" should {
    "be generated successfully" in {
      val duration = DateTimeDuration(t, tPlusOneYear)
      assert(duration.from.isEqual(t))
      assert(duration.to.isEqual(tPlusOneYear))
    }

    "be generated successfully with the same LocalDateTime" in {
      val localDateTime = t
      val duration = DateTimeDuration(localDateTime, localDateTime)
      assert(duration.from.isEqual(localDateTime))
      assert(duration.to.isEqual(localDateTime))
    }

    "not be generated successfully with illegal LocalDateTime" in {
      assertThrows[IllegalArgumentException](DateTimeDuration(t, tPlusOneYear.minusYears(2)))
    }

    "be generated successfully from LocalDate" in {
      require(t.toLocalTime.equals(rebootTime))
      require(tPlusOneYear.toLocalTime.equals(rebootTime))
      val duration = DateTimeDuration.fromLocalDate(dateFrom, dateTo)
      assert(duration.from.isEqual(t))
      assert(duration.to.isEqual(tPlusOneYear))
    }

    "be generated successfully from the same LocalDate" in {
      val localDateTime = t
      val localDate = dateFrom
      require(localDateTime.toLocalTime.equals(rebootTime))
      val duration = DateTimeDuration.fromLocalDate(localDate, localDate)
      assert(duration.from.isEqual(localDateTime))
      assert(duration.to.isEqual(localDateTime))
    }

    "not be generated successfully with illegal LocalDate" in {
      assertThrows[IllegalArgumentException](
        DateTimeDuration.fromLocalDate(dateFrom, dateTo.minusYears(2))
      )
    }
  }

  "DateTimeDuration#contains" when {
    val duration = DateTimeDuration(t, tPlusOneYear)

    "the same as from and to" should {
      "be true" in {
        assert(duration.contains(t))
        assert(duration.contains(tPlusOneYear))
      }
    }
    "shortly after from" should {
      "be true" in assert(duration.contains(t.plusMinutes(1)))
    }

    "before from" should {
      "be false" in assert(!duration.contains(t.minusYears(1)))
    }
    "after to" should {
      "be false" in assert(!duration.contains(tPlusOneYear.plusYears(1)))
    }
  }

  "DateTimeDuration#isEntirelyAfter" when {
    val duration = DateTimeDuration(t, tPlusOneYear)

    "the same as from" should {
      "be true" in assert(duration.isEntirelyAfter(duration.from))
    }
    "before from" should {
      "be true" in assert(duration.isEntirelyAfter(duration.from.minusYears(1)))
    }

    "after from" should {
      "be false" in assert(!duration.isEntirelyAfter(duration.from.plusYears(1)))
    }
  }
}
