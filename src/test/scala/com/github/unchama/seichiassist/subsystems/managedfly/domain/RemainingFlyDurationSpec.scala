package com.github.unchama.seichiassist.subsystems.managedfly.domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class RemainingFlyDurationSpec extends AnyWordSpec with ScalaCheckPropertyChecks with Matchers {
  "RemainingFlyDuration#tickOneMinute" should {
    "decrement remaining minute if greater than 1" in {
      forAll { minute: Int =>
        whenever(minute > 1) {
          RemainingFlyDuration.PositiveMinutes.fromPositive(minute).tickOneMinute shouldBe Some {
            RemainingFlyDuration.PositiveMinutes.fromPositive(minute - 1)
          }
        }
      }
    }

    "tick to None if only a minute remains" in {
      RemainingFlyDuration.PositiveMinutes.fromPositive(1).tickOneMinute
    }

    "fix Infinity" in {
      RemainingFlyDuration.Infinity.tickOneMinute shouldBe Some {
        RemainingFlyDuration.Infinity
      }
    }
  }
}
