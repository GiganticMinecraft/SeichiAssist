package com.github.unchama.seichiassist.subsystems.fairy.domain

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyAppleConsumeStrategy
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.duration._

class FairyAppleConsumeStrategySpec
    extends AnyWordSpec
    with ScalaCheckPropertyChecks
    with Matchers {

  "FairyAppleConsumeStrategy.Permissible" should {
    "任意のdurationでtrueを返す" in {
      Seq(0, 1, 30, 60, 90, 120, 300, 3600).foreach { s =>
        FairyAppleConsumeStrategy.Permissible.isRecoveryTiming(s.seconds) shouldBe true
      }
    }
  }

  "FairyAppleConsumeStrategy.Consume" should {
    "60の倍数秒でtrueを返す" in {
      Seq(60, 120, 180, 300).foreach { s =>
        FairyAppleConsumeStrategy.Consume.isRecoveryTiming(s.seconds) shouldBe true
      }
    }

    "60の倍数でない秒数でfalseを返す" in {
      Seq(30, 61, 90, 119).foreach { s =>
        FairyAppleConsumeStrategy.Consume.isRecoveryTiming(s.seconds) shouldBe false
      }
    }
  }

  "FairyAppleConsumeStrategy.LessConsume" should {
    "90の倍数秒でtrueを返す" in {
      Seq(90, 180, 270, 360).foreach { s =>
        FairyAppleConsumeStrategy.LessConsume.isRecoveryTiming(s.seconds) shouldBe true
      }
    }

    "90の倍数でない秒数でfalseを返す" in {
      Seq(30, 60, 91, 120).foreach { s =>
        FairyAppleConsumeStrategy.LessConsume.isRecoveryTiming(s.seconds) shouldBe false
      }
    }
  }

  "FairyAppleConsumeStrategy.NoConsume" should {
    "120の倍数秒でtrueを返す" in {
      Seq(120, 240, 360, 480).foreach { s =>
        FairyAppleConsumeStrategy.NoConsume.isRecoveryTiming(s.seconds) shouldBe true
      }
    }

    "120の倍数でない秒数でfalseを返す" in {
      Seq(30, 60, 90, 119, 121).foreach { s =>
        FairyAppleConsumeStrategy.NoConsume.isRecoveryTiming(s.seconds) shouldBe false
      }
    }
  }
}
