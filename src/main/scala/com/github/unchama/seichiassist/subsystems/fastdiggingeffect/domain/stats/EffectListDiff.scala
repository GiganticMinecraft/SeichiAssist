package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.stats

import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.{
  FastDiggingAmplifier,
  FastDiggingEffect
}

case class EffectListDiff(
  oldList: Option[List[FastDiggingEffect]],
  newList: List[FastDiggingEffect]
) {

  import cats.implicits._

  lazy val newEffectAmplifier: FastDiggingAmplifier =
    newList.map(_.amplifier).combineAll

  lazy val oldEffectAmplifier: FastDiggingAmplifier =
    oldList.map(_.map(_.amplifier).combineAll).getOrElse(FastDiggingAmplifier.zero)

  lazy val hasDifference: Boolean =
    newEffectAmplifier != oldEffectAmplifier

}
