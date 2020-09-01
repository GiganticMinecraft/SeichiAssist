package com.github.unchama.seichiassist.subsystems.managedfly

import com.github.unchama.concurrent.ReadOnlyRef
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.seichiassist.subsystems.managedfly.domain.RemainingFlyDuration

case class InternalState[F[_]](playerFlyDurations: PlayerDataRepository[ReadOnlyRef[F, Option[RemainingFlyDuration]]])
