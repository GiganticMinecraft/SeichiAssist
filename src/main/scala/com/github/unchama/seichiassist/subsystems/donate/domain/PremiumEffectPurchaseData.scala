package com.github.unchama.seichiassist.subsystems.donate.domain

import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillPremiumEffect

import java.time.LocalDate

abstract class PremiumEffectPurchaseData(val timestamp: LocalDate)

case class Obtained(effectPoint: DonatePremiumEffectPoint, purchaseDate: LocalDate)
    extends PremiumEffectPurchaseData(purchaseDate)

case class Used(
  usePoint: DonatePremiumEffectPoint,
  usedDate: LocalDate,
  forPurchaseOf: ActiveSkillPremiumEffect
) extends PremiumEffectPurchaseData(usedDate)
