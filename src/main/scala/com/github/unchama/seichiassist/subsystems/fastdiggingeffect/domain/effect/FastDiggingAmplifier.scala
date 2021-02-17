package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect

/**
 * @param hasteEffectLevel 付与すべき Haste 効果の強さ。たとえば、この値が 2.0 であれば Haste II が付与される。
 */
case class FastDiggingAmplifier(hasteEffectLevel: Double) {

  def combine(another: FastDiggingAmplifier): FastDiggingAmplifier =
    FastDiggingAmplifier(hasteEffectLevel + another.hasteEffectLevel)

  /**
   * マインクラフトの "amplifier" 値としてこの値を変換する。
   *
   * マインクラフトはポーション効果値を "amplifier" という値を持っているが、
   * たとえば Haste II に対応する "amplifier" 値は 1 である。
   * このように、表示される値より 1 少ない値を内部的に保持しているため、
   * [[hasteEffectLevel]] から1を引いた数を切り捨て、0とのmaxを取っている。
   */
  def toMinecraftPotionAmplifier: Int = (hasteEffectLevel - 1).floor.toInt max 0

}
