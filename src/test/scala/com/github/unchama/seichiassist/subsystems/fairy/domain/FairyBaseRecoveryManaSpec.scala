package com.github.unchama.seichiassist.subsystems.fairy.domain

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyBaseRecoveryMana
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FairyBaseRecoveryManaSpec extends AnyWordSpec with Matchers {

  "FairyBaseRecoveryMana.manaAmountAt" should {

    "levelCappedManaAmount = 0 のとき結果が 200 になる" in {
      val result = FairyBaseRecoveryMana.manaAmountAt(0.0, 0.0)
      result.amount shouldBe 200
    }

    "randomRoll = 0.0 のとき最小値が 200 以上になる" in {
      val result = FairyBaseRecoveryMana.manaAmountAt(3000.0, 0.0)
      result.amount should be >= 200
    }

    "randomRoll = 0.9999 のとき最大値が期待範囲内になる" in {
      // levelCappedManaAmount = 3000 の場合:
      //   base    = 3000/10 - 3000/30 = 300 - 100 = 200
      //   maxJitterSteps = (3000/20).toInt = 150
      //   jitter  = 150 * 0.9999 / 2.9 ≈ 51.71
      //   result  = floor(200 + 51.71) + 200 = 251 + 200 = 451
      val result = FairyBaseRecoveryMana.manaAmountAt(3000.0, 0.9999)
      result.amount should (be >= 200 and be <= 500)
    }

    "randomRoll が大きいほど amount が大きくなる（単調性）" in {
      val low = FairyBaseRecoveryMana.manaAmountAt(3000.0, 0.1)
      val high = FairyBaseRecoveryMana.manaAmountAt(3000.0, 0.9)
      high.amount should be >= low.amount
    }

    "負の levelCappedManaAmount は例外を投げる" in {
      an[IllegalArgumentException] should be thrownBy {
        FairyBaseRecoveryMana.manaAmountAt(-1.0, 0.5)
      }
    }

    "randomRoll < 0.0 は例外を投げる" in {
      an[IllegalArgumentException] should be thrownBy {
        FairyBaseRecoveryMana.manaAmountAt(1000.0, -0.1)
      }
    }

    "randomRoll >= 1.0 は例外を投げる" in {
      an[IllegalArgumentException] should be thrownBy {
        FairyBaseRecoveryMana.manaAmountAt(1000.0, 1.0)
      }
    }
  }
}
