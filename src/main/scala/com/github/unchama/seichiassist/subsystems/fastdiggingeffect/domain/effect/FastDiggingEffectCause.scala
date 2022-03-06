package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount

/**
 * 採掘速度上昇効果の要因
 */
sealed abstract class FastDiggingEffectCause(val description: String)

object FastDiggingEffectCause {

  case class FromConnectionNumber(number: Int)
      extends FastDiggingEffectCause(s"接続人数(${number}人)")

  case class FromMinuteBreakCount(seichiExpAmount: SeichiExpAmount)
      extends FastDiggingEffectCause(s"1分間の整地量(${seichiExpAmount.amount})")

  case object FromDragonNightTime extends FastDiggingEffectCause("ドラゲナイタイムから")

}
