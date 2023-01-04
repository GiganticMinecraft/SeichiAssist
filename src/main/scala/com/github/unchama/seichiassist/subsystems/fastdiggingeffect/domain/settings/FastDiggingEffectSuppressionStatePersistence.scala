package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings

import com.github.unchama.generic.RefDict

import java.util.UUID

trait FastDiggingEffectSuppressionStatePersistence[F[_]]
    extends RefDict[F, UUID, FastDiggingEffectSuppressionState]
