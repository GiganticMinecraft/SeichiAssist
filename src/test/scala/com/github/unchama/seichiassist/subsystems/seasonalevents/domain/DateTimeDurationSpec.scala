package com.github.unchama.seichiassist.subsystems.seasonalevents.domain

import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDateTime

class DateTimeDurationSpec extends AnyWordSpec {
  private val from = LocalDateTime.of(2022, 1, 1, 4, 10)
  private val to = LocalDateTime.of(2023, 1, 1, 4, 10)
  private val dateFrom = from.toLocalDate
  private val dateTo = to.toLocalDate

  "DateTimeDuration" should {
    "be generated successfully" in {
      val duration = DateTimeDuration(from, to)
      assert(duration.from.isEqual(from))
      assert(duration.to.isEqual(to))
    }

    "be generated successfully with the same LocalDateTime" in {
      val duration = DateTimeDuration(from, to.minusYears(1))
      assert(duration.from.isEqual(from))
      assert(duration.to.isEqual(to.minusYears(1)))
    }

    "not be generated successfully with illegal LocalDateTime" in {
      assertThrows[IllegalArgumentException](DateTimeDuration(from, to.minusYears(2)))
    }

    "be generated successfully from LocalDate" in {
      val duration = DateTimeDuration.fromLocalDate(dateFrom, dateTo)
      assert(duration.from.isEqual(from))
      assert(duration.to.isEqual(to))
    }

    "be generated successfully from the same LocalDate" in {
      val duration = DateTimeDuration.fromLocalDate(dateFrom, dateTo.minusYears(1))
      assert(duration.from.isEqual(from))
      assert(duration.to.isEqual(to.minusYears(1)))
    }

    "not be generated successfully with illegal LocalDate" in {
      assertThrows[IllegalArgumentException](DateTimeDuration.fromLocalDate(dateFrom, dateTo.minusYears(2)))
    }
  }

  "DateTimeDuration#contains" when {
    val duration = DateTimeDuration(from, to)

    "the same as from and to" should {
      "be true" in {
        assert(duration.contains(from))
        assert(duration.contains(to))
      }
    }
    "shortly after from" should {
      "be true" in assert(duration.contains(from.plusMinutes(1)))
    }

    "before from" should {
      "be false" in assert(!duration.contains(from.minusYears(1)))
    }
    "after to" should {
      "be false" in assert(!duration.contains(to.plusYears(1)))
    }
  }

  "DateTimeDuration#isEntirelyAfter" when {
    val duration = DateTimeDuration(from, to)

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
