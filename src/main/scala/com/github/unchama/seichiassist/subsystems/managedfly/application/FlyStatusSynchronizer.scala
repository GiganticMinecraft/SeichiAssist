package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.Monad
import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.concurrent.MinecraftServerThreadShift
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.managedfly.domain.PlayerFlyStatus

abstract class FlyStatusSynchronizer[
  AsyncContext[_] : Monad : MinecraftServerThreadShift,
  SyncContext[_] : ContextCoercion[*[_], AsyncContext] : Sync,
  Player
](implicit repository: KeyedDataRepository[Player, Ref[SyncContext, PlayerFlyStatus]]) {

  def applyFlyStatusToMinecraftEntity(player: Player, status: PlayerFlyStatus): AsyncContext[Unit]

  def setFlyStatus(player: Player, status: PlayerFlyStatus): AsyncContext[Unit] = {
    import ContextCoercion._
    import cats.implicits._

    for {
      _ <- repository(player).set(status).coerceTo[AsyncContext]
      _ <- applyFlyStatusToMinecraftEntity(player, status)
    } yield ()
  }

}
