package com.github.unchama.seichiassist.subsystems.managedfly.application

import com.github.unchama.seichiassist.subsystems.managedfly.domain.PlayerFlyStatus

trait FlyStatusSynchronizer[F[_], Player] {

  def setFlyStatus(player: Player, status: PlayerFlyStatus): F[Unit]

}
