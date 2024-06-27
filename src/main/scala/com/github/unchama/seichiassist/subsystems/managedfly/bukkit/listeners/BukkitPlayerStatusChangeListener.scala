package com.github.unchama.seichiassist.subsystems.managedfly.bukkit.listeners

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, SyncEffect, SyncIO}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.managedfly.application.{
  ActiveSessionReference,
  PlayerFlyStatusManipulation
}
import org.bukkit.entity.Player
import org.bukkit.event.player.{PlayerChangedWorldEvent, PlayerRespawnEvent}
import org.bukkit.event.{EventHandler, Listener}

class BukkitPlayerStatusChangeListener[F[_]: ConcurrentEffect, G[_]: SyncEffect](
  implicit
  sessionReferenceRepository: KeyedDataRepository[Player, ActiveSessionReference[F, G]],
  playerFlyStatusManipulation: PlayerFlyStatusManipulation[Kleisli[F, Player, *]]
) extends Listener {

  import cats.effect.implicits._

  @EventHandler
  def onWorldChange(event: PlayerChangedWorldEvent): Unit = {
    val player = event.getPlayer

    val program = for {
      currentStatus <- sessionReferenceRepository(player)
        .getLatestFlyStatus
        .runSync[SyncIO]
        .toIO
      _ <- playerFlyStatusManipulation.synchronizeFlyStatus(currentStatus)(player).toIO
    } yield ()

    program.unsafeRunAsyncAndForget()
  }

  @EventHandler
  def onPlayerRespawn(event: PlayerRespawnEvent): Unit = {
    val player = event.getPlayer

    val program = for {
      currentStatus <- sessionReferenceRepository(player)
        .getLatestFlyStatus
        .runSync[SyncIO]
        .toIO
      _ <- playerFlyStatusManipulation.synchronizeFlyStatus(currentStatus)(player).toIO
    } yield ()

    program.unsafeRunAsyncAndForget()
  }

}
