package com.github.unchama.seichiassist.subsystems.donate.domain

import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillPremiumEffect

import java.time.LocalDate

sealed trait PremiumEffectPurchaseData

case class Obtained(effectPoint: DonatePremiumEffectPoint, purchaseDate: LocalDate)
    extends PremiumEffectPurchaseData

case class Used(
  usePoint: DonatePremiumEffectPoint,
  usedDate: LocalDate,
  forPurchaseOf: ActiveSkillPremiumEffect
) extends PremiumEffectPurchaseData
