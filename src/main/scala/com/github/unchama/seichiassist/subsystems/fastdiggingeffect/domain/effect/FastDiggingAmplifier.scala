package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect

import cats.kernel.{Monoid, Order}

/**
 * @param hasteEffectLevel
 *   付与すべき Haste 効果の強さ。たとえば、この値が 2.0 であれば Haste II が付与される。
 */
case class FastDiggingAmplifier(hasteEffectLevel: Double) {

  def combine(another: FastDiggingAmplifier): FastDiggingAmplifier =
    FastDiggingAmplifier(hasteEffectLevel + another.hasteEffectLevel)

  /**
   * 表示用に小数点以下2桁までにフォーマットした文字列。
   */
  val formatted: String = String.format("%,.2f", hasteEffectLevel)

  /**
   * [[hasteEffectLevel]] を切り捨て、 0 とのmaxを取った値。 ポーション効果の強さとしてクライアントのUIに表示される値と同じ値が得られる。
   */
  val normalizedEffectLevel: Int = hasteEffectLevel.floor.toInt max 0

  /**
   * マインクラフトの "amplifier" 値としてこの値を変換する。
   *
   * マインクラフトはポーション効果値を "amplifier" という値を持っているが、 たとえば Haste II に対応する "amplifier" 値は 1 である。
   *
   * このように、表示される値より 1 少ない値を内部的に保持しているため、 この関数では [[normalizedEffectLevel]] から1を引いた数と0とのmaxを取っている。
   *
   * [[hasteEffectLevel]] が一未満、つまり [[normalizedEffectLevel]] が0以下であるならば、 [[None]] となる。
   */
  val toMinecraftPotionAmplifier: Option[Int] = {
    val level = normalizedEffectLevel - 1
    Option.when(level >= 0)(level)
  }

}

object FastDiggingAmplifier {

  final val zero = FastDiggingAmplifier(0)

  implicit val monoid: Monoid[FastDiggingAmplifier] =
    Monoid.instance(zero, (a, b) => a.combine(b))

  implicit val order: Order[FastDiggingAmplifier] =
    Order.by(_.hasteEffectLevel)

}
