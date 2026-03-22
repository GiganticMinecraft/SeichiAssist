package com.github.unchama.seichiassist.subsystems.fairy.domain

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairyManaRecovery
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  FairyManaRecoveryState,
  FairyRecoveryMana
}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class FairyManaRecoverySpec extends AnyWordSpec with ScalaCheckPropertyChecks with Matchers {

  "FairyManaRecovery.compute" should {

    "recoveryMana < 300 のとき (pureAppleConsumeAmount == 0) finalAmount == 0.0 を返す" in {
      val result = FairyManaRecovery.compute(FairyRecoveryMana(299), 100L, 0.5, isDragonNight = false)
      result.finalAmount shouldBe 0.0
      result.appleConsumed shouldBe 0
    }

    "bonusRoll <= 0.03 のときボーナスが適用される" in {
      val mana = FairyRecoveryMana(600) // pureAppleConsumeAmount = 2
      val withBonus = FairyManaRecovery.compute(mana, 10L, 0.03, isDragonNight = false)
      val withoutBonus = FairyManaRecovery.compute(mana, 10L, 0.04, isDragonNight = false)
      withBonus.finalAmount should be > withoutBonus.finalAmount
    }

    "bonusRoll > 0.03 のときボーナスなし" in {
      val mana = FairyRecoveryMana(600)
      val result = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      val baseAmount = 600 * 0.7
      result.manaFromApples shouldBe baseAmount +- 0.001
    }

    "isDragonNight = true のとき finalAmount が2倍になる" in {
      val mana = FairyRecoveryMana(600)
      val normal = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      val dragon = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = true)
      dragon.finalAmount shouldBe normal.finalAmount * 2.0 +- 0.001
    }

    "isDragonNight = false のとき乗算なし (finalAmount == manaFromApples)" in {
      val mana = FairyRecoveryMana(600)
      val result = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      result.finalAmount shouldBe result.manaFromApples +- 0.001
    }

    "mineStackedAmount < pureAppleConsumeAmount のとき appleConsumed がスタック量に丸められる" in {
      val mana = FairyRecoveryMana(900) // pureAppleConsumeAmount = 3
      val result = FairyManaRecovery.compute(mana, 1L, 0.5, isDragonNight = false)
      result.appleConsumed shouldBe 1
    }

    "mineStackedAmount >= pureAppleConsumeAmount のとき appleConsumed が pure量" in {
      val mana = FairyRecoveryMana(900) // pureAppleConsumeAmount = 3
      val result = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      result.appleConsumed shouldBe 3
    }

    "state: りんごを消費して回復したとき RecoveredWithApple" in {
      val mana = FairyRecoveryMana(600)
      val result = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      result.state shouldBe FairyManaRecoveryState.RecoveredWithApple
    }

    "state: MineStackが0のとき RecoveredWithoutApple" in {
      val mana = FairyRecoveryMana(600)
      val result = FairyManaRecovery.compute(mana, 0L, 0.5, isDragonNight = false)
      result.state shouldBe FairyManaRecoveryState.RecoveredWithoutApple
    }

    "state: recoveryMana == 0 かつ manaFromApples < 300 のとき RecoverWithoutAppleButLessThanAApple" in {
      val mana = FairyRecoveryMana(0)
      val result = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      result.state shouldBe FairyManaRecoveryState.RecoverWithoutAppleButLessThanAApple
    }
  }
}
