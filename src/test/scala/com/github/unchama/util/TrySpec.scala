package com.github.unchama.util

import java.util.Optional

import com.github.unchama.util.failable.{FailableAction, Try}
import org.scalatest.wordspec.AnyWordSpec

class TrySpec extends AnyWordSpec {
  "successful try" should {
    val result = Try.sequence(new FailableAction(1, () => ActionStatus.Ok))

    "have ok status" in {
      assert(result.overallStatus() == ActionStatus.Ok)
    }

    "have no failed value" in {
      assert(result.failedValue() == Optional.empty())
    }
  }

  "failed try" should {
    val result = Try.sequence(new FailableAction(2, () => ActionStatus.Fail))

    "have fail status" in {
      assert(result.overallStatus() == ActionStatus.Fail)
    }

    "have provided fail value" in {
      assert(result.failedValue() == Optional.of(2))
    }
  }
}
