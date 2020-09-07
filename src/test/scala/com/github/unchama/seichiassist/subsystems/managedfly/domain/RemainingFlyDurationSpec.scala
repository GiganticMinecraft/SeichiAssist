package com.github.unchama.seichiassist.subsystems.managedfly.domain

import org.scalacheck.Gen
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.annotation.tailrec

class RemainingFlyDurationSpec extends AnyWordSpec with ScalaCheckPropertyChecks with Matchers {
  @tailrec
  private def repeat[A](f: A => A, n: Int)(a: A): A = {
    n match {
      case _ if n < 1 => a
      case _ => repeat(f, n - 1)(f(a))
    }
  }

  "RemainingFlyDuration" should {
    "tick to None in exactly the given minute amount" in {
      val testMinuteLimit = 1000000

      forAll(Gen.choose(1, testMinuteLimit)) { minute: Int =>
        val tickOneMinute: Option[RemainingFlyDuration] => Option[RemainingFlyDuration] = _.flatMap(_.tickOneMinute)
        val duration: Option[RemainingFlyDuration] = Some(RemainingFlyDuration.PositiveMinutes.fromPositive(minute))

        val secondLast = repeat(tickOneMinute, minute - 1)(duration)
        val last = tickOneMinute(secondLast)

        secondLast shouldBe Some(RemainingFlyDuration.PositiveMinutes.fromPositive(1))
        last shouldBe None
      }
    }
  }
}
