package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain

import com.github.unchama.generic.RefDict

import java.util.UUID

trait BreakSkillTargetConfigPersistence[F[_]] extends RefDict[F, UUID, BreakSkillTargetConfig]
