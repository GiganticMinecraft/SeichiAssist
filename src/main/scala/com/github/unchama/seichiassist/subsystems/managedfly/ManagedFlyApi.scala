package com.github.unchama.seichiassist.subsystems.managedfly

import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.subsystems.managedfly.domain.PlayerFlyStatus

trait ManagedFlyApi[G[_], Player] {

  val playerFlyDurations: KeyedDataRepository[Player, ReadOnlyRef[G, PlayerFlyStatus]]

}
