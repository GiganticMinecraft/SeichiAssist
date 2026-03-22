package com.github.unchama.seichiassist.subsystems.fairy.domain

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairyManaRecovery
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  FairyManaRecoveryState,
  FairyBaseRecoveryMana
}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class FairyManaRecoverySpec extends AnyWordSpec with ScalaCheckPropertyChecks with Matchers {

  "FairyManaRecovery.compute" should {

    "recoveryMana < 300 のとき (pureAppleConsumeAmount == 0) finalRecoveredMana == 0.0 を返す" in {
      val result = FairyManaRecovery.compute(FairyBaseRecoveryMana(299), 100L, 0.5, isDragonNight = false)
      result.finalRecoveredMana shouldBe 0.0
      result.consumedGachaAppleCount shouldBe 0
    }

    "bonusRoll <= 0.03 のときボーナスが適用される" in {
      val mana = FairyBaseRecoveryMana(600) // pureAppleConsumeAmount = 2
      val withBonus = FairyManaRecovery.compute(mana, 10L, 0.03, isDragonNight = false)
      val withoutBonus = FairyManaRecovery.compute(mana, 10L, 0.04, isDragonNight = false)
      withBonus.finalRecoveredMana should be > withoutBonus.finalRecoveredMana
    }

    "bonusRoll > 0.03 のときボーナスなし" in {
      val mana = FairyBaseRecoveryMana(600)
      val result = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      val baseAmount = 600 * 0.7
      result.manaBeforeDragonNightMultiplier shouldBe baseAmount +- 0.001
    }

    "isDragonNight = true のとき finalRecoveredMana が2倍になる" in {
      val mana = FairyBaseRecoveryMana(600)
      val normal = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      val dragon = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = true)
      dragon.finalRecoveredMana shouldBe normal.finalRecoveredMana * 2.0 +- 0.001
    }

    "isDragonNight = false のとき乗算なし (finalRecoveredMana == manaBeforeDragonNightMultiplier)" in {
      val mana = FairyBaseRecoveryMana(600)
      val result = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      result.finalRecoveredMana shouldBe result.manaBeforeDragonNightMultiplier +- 0.001
    }

    "mineStackedAmount < pureAppleConsumeAmount のとき appleConsumed がスタック量に丸められる" in {
      val mana = FairyBaseRecoveryMana(900) // pureAppleConsumeAmount = 3
      val result = FairyManaRecovery.compute(mana, 1L, 0.5, isDragonNight = false)
      result.consumedGachaAppleCount shouldBe 1
    }

    "mineStackedAmount >= pureAppleConsumeAmount のとき appleConsumed が pure量" in {
      val mana = FairyBaseRecoveryMana(900) // pureAppleConsumeAmount = 3
      val result = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      result.consumedGachaAppleCount shouldBe 3
    }

    "state: りんごを消費して回復したとき RecoveredWithApple" in {
      val mana = FairyBaseRecoveryMana(600)
      val result = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      result.state shouldBe FairyManaRecoveryState.RecoveredWithApple
    }

    "state: MineStackが0のとき RecoveredWithoutApple" in {
      val mana = FairyBaseRecoveryMana(600)
      val result = FairyManaRecovery.compute(mana, 0L, 0.5, isDragonNight = false)
      result.state shouldBe FairyManaRecoveryState.RecoveredWithoutApple
    }

    "state: recoveryMana == 0 かつ manaBeforeDragonNightMultiplier < 300 のとき RecoverWithoutAppleButLessThanAApple" in {
      val mana = FairyBaseRecoveryMana(0)
      val result = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      result.state shouldBe FairyManaRecoveryState.RecoverWithoutAppleButLessThanAApple
    }
  }
}
