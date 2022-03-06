package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.stats

sealed trait FastDiggingEffectStatsSettings {

  final lazy val nextValue: FastDiggingEffectStatsSettings =
    this match {
      case FastDiggingEffectStatsSettings.AlwaysReceiveDetails =>
        FastDiggingEffectStatsSettings.ReceiveTotalAmplifierOnUpdate
      case FastDiggingEffectStatsSettings.ReceiveTotalAmplifierOnUpdate =>
        FastDiggingEffectStatsSettings.AlwaysReceiveDetails
    }

}

object FastDiggingEffectStatsSettings {

  case object AlwaysReceiveDetails extends FastDiggingEffectStatsSettings

  case object ReceiveTotalAmplifierOnUpdate extends FastDiggingEffectStatsSettings

}
