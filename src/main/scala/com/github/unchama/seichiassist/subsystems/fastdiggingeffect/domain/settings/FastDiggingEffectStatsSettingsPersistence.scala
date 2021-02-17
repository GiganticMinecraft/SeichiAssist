package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings

import com.github.unchama.generic.RefDict

import java.util.UUID

trait FastDiggingEffectStatsSettingsPersistence[F[_]]
  extends RefDict[F, UUID, FastDiggingEffectStatsSettings]
