package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect

/**
 * 単一の採掘速度上昇効果。 [[amplifier]]はマインクラフトの "Haste" ポーション効果と同等のスケールを持つ数値である。
 */
case class FastDiggingEffect(amplifier: FastDiggingAmplifier, cause: FastDiggingEffectCause)
