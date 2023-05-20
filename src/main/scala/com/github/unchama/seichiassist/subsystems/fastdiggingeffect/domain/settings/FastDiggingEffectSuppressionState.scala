package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings

import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.FastDiggingAmplifier

sealed trait FastDiggingEffectSuppressionState {

  import FastDiggingEffectSuppressionState._

  lazy val nextState: FastDiggingEffectSuppressionState =
    this match {
      case EnabledWithoutLimit     => EnabledWithLimit.Of_127
      case EnabledWithLimit.Of_127 => EnabledWithLimit.Of_200
      case EnabledWithLimit.Of_200 => EnabledWithLimit.Of_400
      case EnabledWithLimit.Of_400 => EnabledWithLimit.Of_600
      case EnabledWithLimit.Of_600 => Disabled
      case Disabled                => EnabledWithoutLimit
    }

  lazy val effectAmplifierCap: FastDiggingAmplifier = {
    FastDiggingAmplifier {
      this match {
        case EnabledWithoutLimit         => 65535
        case withLimit: EnabledWithLimit => withLimit.limit
        case Disabled                    => 0
      }
    }
  }
}

object FastDiggingEffectSuppressionState {

  case object EnabledWithoutLimit extends FastDiggingEffectSuppressionState

  abstract sealed class EnabledWithLimit(val limit: Int)
      extends FastDiggingEffectSuppressionState

  object EnabledWithLimit {

    case object Of_127 extends EnabledWithLimit(127)

    case object Of_200 extends EnabledWithLimit(200)

    case object Of_400 extends EnabledWithLimit(400)

    case object Of_600 extends EnabledWithLimit(600)

  }

  case object Disabled extends FastDiggingEffectSuppressionState

}
