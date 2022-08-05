package com.github.unchama.seichiassist.subsystems.donate.domain

import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillPremiumEffect

import java.time.LocalDate

sealed trait PremiumEffectPurchaseData

object PremiumEffectPurchaseData {

  case class Obtained(usePoint: Int, purchaseDate: LocalDate)
      extends PremiumEffectPurchaseData {
    require(usePoint >= 0, "usePointは非負の値で指定してください。")
  }

  case class Used(usePoint: Int, usedDate: LocalDate, forPurchaseOf: ActiveSkillPremiumEffect)
      extends PremiumEffectPurchaseData {
    require(usePoint >= 0, "usePointは非負の値で指定してください。")
  }

}
