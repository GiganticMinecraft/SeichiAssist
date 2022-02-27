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

    "be generated successfully with same DateTime" in {
      val duration = DateTimeDuration(from, to.minusYears(1))
      assert(duration.from.isEqual(from))
      assert(duration.to.isEqual(to.minusYears(1)))
    }

    "be failed to generate with illegal arg" in {
      assertThrows[IllegalArgumentException](DateTimeDuration(from, to.minusYears(2)))
    }

    "be generated successfully from LocalDate" in {
      val duration = DateTimeDuration.fromLocalDate(dateFrom, dateTo)
      assert(duration.from.isEqual(from))
      assert(duration.to.isEqual(to))
    }

    "be generated successfully from same LocalDate" in {
      val duration = DateTimeDuration.fromLocalDate(dateFrom, dateTo.minusYears(1))
      assert(duration.from.isEqual(from))
      assert(duration.to.isEqual(to.minusYears(1)))
    }

    "be failed to generate with illegal arg" in {
      assertThrows[IllegalArgumentException](DateTimeDuration.fromLocalDate(dateFrom, dateTo.minusYears(2)))
    }
  }
}
