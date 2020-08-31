package com.github.unchama.seichiassist.subsystems.managedfly.bukkit

import cats.Monad
import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.concurrent.MinecraftServerThreadShift
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.playerdatarepository.PlayerDataRepository
import com.github.unchama.seichiassist.subsystems.managedfly.application.FlyStatusSynchronizer
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{Flying, NotFlying, PlayerFlyStatus}
import org.bukkit.entity.Player

class FlyStatusSynchronizerImpl[
  AsyncContext[_] : Monad : MinecraftServerThreadShift,
  SyncContext[_] : ContextCoercion[*[_], AsyncContext] : Sync
](implicit repository: PlayerDataRepository[Ref[SyncContext, PlayerFlyStatus]])
  extends FlyStatusSynchronizer[AsyncContext, Player] {

  import ContextCoercion._
  import cats.implicits._

  override def setFlyStatus(player: Player, status: PlayerFlyStatus): AsyncContext[Unit] = {
    for {
      _ <- repository(player).set(status).coerceTo[AsyncContext]
      _ <- MinecraftServerThreadShift[AsyncContext].shift
      _ <- Sync[SyncContext].delay {
        status match {
          case Flying(_) =>
            player.setAllowFlight(true)
            player.setFlying(true)
          case NotFlying =>
            player.setAllowFlight(false)
            player.setFlying(false)
        }
      }.coerceTo[AsyncContext]
    } yield ()
  }

}
