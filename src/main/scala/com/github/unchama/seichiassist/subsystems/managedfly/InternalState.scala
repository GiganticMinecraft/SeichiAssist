package com.github.unchama.seichiassist.subsystems.managedfly

import cats.effect.concurrent.Ref
import com.github.unchama.playerdatarepository.PlayerDataRepository
import com.github.unchama.seichiassist.subsystems.managedfly.domain.RemainingFlyDuration

case class InternalState[F[_]](playerFlyDurations: PlayerDataRepository[Ref[F, Option[RemainingFlyDuration]]])
