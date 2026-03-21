package com.github.unchama.seichiassist.subsystems.dragonnighttime.domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.LocalTime
import scala.concurrent.duration._

class PeriodSpec extends AnyWordSpec with ScalaCheckPropertyChecks with Matchers {
  val start: LocalTime = LocalTime.of(20, 0, 0)
  val end: LocalTime = LocalTime.of(21, 0, 0)
  val period: Period = Period(start, end)

  "Period" should {
    "startAt >= endAt のとき例外を投げる" in {
      intercept[IllegalArgumentException] {
        Period(LocalTime.of(21, 0, 0), LocalTime.of(20, 0, 0))
      }
      intercept[IllegalArgumentException] {
        Period(LocalTime.of(20, 0, 0), LocalTime.of(20, 0, 0))
      }
    }

    "contains は startAt より前のとき false を返す" in {
      period.contains(LocalTime.of(19, 59, 59)) shouldBe false
    }

    "contains は startAt ちょうどのとき true を返す" in {
      period.contains(start) shouldBe true
    }

    "contains は期間内のとき true を返す" in {
      period.contains(LocalTime.of(20, 30, 0)) shouldBe true
    }

    "contains は endAt ちょうどのとき false を返す" in {
      period.contains(end) shouldBe false
    }

    "contains は endAt より後のとき false を返す" in {
      period.contains(LocalTime.of(21, 0, 1)) shouldBe false
    }

    "toFiniteDuration は endAt - startAt の長さを返す" in {
      period.toFiniteDuration shouldBe 3600.seconds
    }

    "remainingDuration は期間内のとき Some(endAt - time) を返す" in {
      val time = LocalTime.of(20, 30, 0)
      period.remainingDuration(time) shouldBe Some(1800.seconds)
    }

    "remainingDuration は startAt ちょうどのとき Some を返す" in {
      period.remainingDuration(start) shouldBe Some(3600.seconds)
    }

    "remainingDuration は期間外のとき None を返す" in {
      period.remainingDuration(LocalTime.of(19, 0, 0)) shouldBe None
      period.remainingDuration(end) shouldBe None
      period.remainingDuration(LocalTime.of(22, 0, 0)) shouldBe None
    }
  }
}
