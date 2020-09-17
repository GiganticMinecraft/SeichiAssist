package com.github.unchama.seichiassist.subsystems.managedfly

import com.github.unchama.concurrent.ReadOnlyRef
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.seichiassist.subsystems.managedfly.domain.PlayerFlyStatus

case class InternalState[SyncContext[_]](playerFlyDurations: PlayerDataRepository[ReadOnlyRef[SyncContext, PlayerFlyStatus]])
