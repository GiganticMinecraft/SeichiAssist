package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.stats

sealed trait FastDiggingEffectStatsSettings {

  final lazy val nextValue: FastDiggingEffectStatsSettings =
    this match {
      case FastDiggingEffectStatsSettings.AlwaysReceive => FastDiggingEffectStatsSettings.ReceiveOnUpdate
      case FastDiggingEffectStatsSettings.ReceiveOnUpdate => FastDiggingEffectStatsSettings.AlwaysReceive
    }

}

object FastDiggingEffectStatsSettings {

  case object AlwaysReceive extends FastDiggingEffectStatsSettings

  case object ReceiveOnUpdate extends FastDiggingEffectStatsSettings

}
