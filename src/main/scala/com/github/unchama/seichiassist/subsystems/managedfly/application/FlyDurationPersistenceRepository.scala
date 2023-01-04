package com.github.unchama.seichiassist.subsystems.managedfly.application

import com.github.unchama.generic.RefDict
import com.github.unchama.seichiassist.subsystems.managedfly.domain.RemainingFlyDuration

import java.util.UUID

trait FlyDurationPersistenceRepository[F[_]]
    extends RefDict[F, UUID, Option[RemainingFlyDuration]]
