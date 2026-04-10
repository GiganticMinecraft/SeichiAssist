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

    "recoveryMana < 300 のとき、がちゃりんごを消費せずに回復する" in {
      val mana = FairyBaseRecoveryMana(299)
      val baseManaRecoveryRatio = 0.7
      val result =
        FairyManaRecovery.compute(mana, 100L, 0.5, isDragonNight = false)
      result.finalRecoveredMana shouldBe mana.amount * baseManaRecoveryRatio +- 0.001
      result.consumedGachaAppleCount shouldBe 0
      result.state shouldBe FairyManaRecoveryState.RecoverWithoutAppleButLessThanAApple
    }

    "recoveryMana < 300 のとき、がちゃりんご在庫が0でも無消費で回復する" in {
      val mana = FairyBaseRecoveryMana(299)
      val baseManaRecoveryRatio = 0.7
      val result =
        FairyManaRecovery.compute(mana, 0L, 0.5, isDragonNight = false)
      result.finalRecoveredMana shouldBe mana.amount * baseManaRecoveryRatio +- 0.001
      result.consumedGachaAppleCount shouldBe 0
      result.state shouldBe FairyManaRecoveryState.RecoverWithoutAppleButLessThanAApple
    }

    "bonusRoll <= 0.03 のときボーナスが適用される" in {
      val mana = FairyBaseRecoveryMana(600) // pureAppleConsumeAmount = 2
      val withBonus = FairyManaRecovery.compute(mana, 10L, 0.03, isDragonNight = false)
      val withoutBonus = FairyManaRecovery.compute(mana, 10L, 0.04, isDragonNight = false)
      withBonus.finalRecoveredMana should be > withoutBonus.finalRecoveredMana
    }

    "在庫不足時でもボーナス回復量は実際に消費したがちゃりんご分だけ減衰しない" in {
      val mana = FairyBaseRecoveryMana(600) // pureAppleConsumeAmount = 2
      val baseManaRecoveryRatio = 0.7
      val bonusManaPerApple = 0.3
      val result = FairyManaRecovery.compute(mana, 1L, 0.03, isDragonNight = false)

      result.manaBeforeDragonNightMultiplier shouldBe
        ((mana.amount * baseManaRecoveryRatio) * 0.5 + bonusManaPerApple) +- 0.001
    }

    "bonusRoll > 0.03 のときボーナスなし" in {
      val mana = FairyBaseRecoveryMana(600)
      val result = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      val baseAmount = 600 * 0.7
      result.manaBeforeDragonNightMultiplier shouldBe baseAmount +- 0.001
    }

    "isDragonNight = true のときマナ回復量が2倍になる" in {
      val mana = FairyBaseRecoveryMana(600)
      val normal = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      val dragon = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = true)
      dragon.finalRecoveredMana shouldBe normal.finalRecoveredMana * 2.0 +- 0.001
    }

    "isDragonNight = false のときボーナスが付かない" in {
      val mana = FairyBaseRecoveryMana(600)
      val result = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      result.finalRecoveredMana shouldBe result.manaBeforeDragonNightMultiplier +- 0.001
    }

    "mineStackedAmount < pureAppleConsumeAmount のとき、がちゃりんご消費数が持っている分に丸められる" in {
      val mana = FairyBaseRecoveryMana(900) // pureAppleConsumeAmount = 3
      val result = FairyManaRecovery.compute(mana, 1L, 0.5, isDragonNight = false)
      result.consumedGachaAppleCount shouldBe 1
    }

    "mineStackedAmount >= pureAppleConsumeAmount のときがちゃりんごの消費数がベース回復量分になる" in {
      val mana = FairyBaseRecoveryMana(900) // pureAppleConsumeAmount = 3
      val result = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      result.consumedGachaAppleCount shouldBe 3
    }

    "state: りんごを消費して回復したとき RecoveredWithApple" in {
      val mana = FairyBaseRecoveryMana(600)
      val result = FairyManaRecovery.compute(mana, 10L, 0.5, isDragonNight = false)
      result.state shouldBe FairyManaRecoveryState.RecoveredWithApple
    }

    "state: MineStack に存在するがちゃりんごの数が0のとき RecoveredWithoutApple" in {
      val mana = FairyBaseRecoveryMana(600)
      val result = FairyManaRecovery.compute(mana, 0L, 0.5, isDragonNight = false)
      result.state shouldBe FairyManaRecoveryState.RecoveredWithoutApple
    }
  }
}
