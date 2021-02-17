package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings

sealed trait FastDiggingEffectStatsSettings {

  final lazy val nextValue: FastDiggingEffectStatsSettings =
    this match {
      case FastDiggingEffectStatsSettings.Receive => FastDiggingEffectStatsSettings.Mute
      case FastDiggingEffectStatsSettings.Mute => FastDiggingEffectStatsSettings.Receive
    }

}

object FastDiggingEffectStatsSettings {

  case object Receive extends FastDiggingEffectStatsSettings

  case object Mute extends FastDiggingEffectStatsSettings

}
