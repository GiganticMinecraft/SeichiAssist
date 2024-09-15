package com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig.domain

import com.github.unchama.generic.RefDict

import java.util.UUID

trait BreakSkillTriggerConfigPersistence[F[_]] extends RefDict[F, UUID, BreakSkillTriggerConfig]
