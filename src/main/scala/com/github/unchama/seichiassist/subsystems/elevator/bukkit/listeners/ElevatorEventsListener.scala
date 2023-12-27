package com.github.unchama.seichiassist.subsystems.elevator.bukkit.listeners

import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.generic.ApplicativeExtra
import org.bukkit.Location
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.event.player.{PlayerMoveEvent, PlayerToggleSneakEvent}
import com.github.unchama.seichiassist.subsystems.elevator.application.actions.FindTeleportLocation

class ElevatorEventsListener[F[_]: ConcurrentEffect](
  implicit findTeleportLocation: FindTeleportLocation[F, Location]
) extends Listener {

  import cats.implicits._
  import cats.effect.implicits._

  @EventHandler
  def onJump(e: PlayerMoveEvent): Unit = {
    val player = e.getPlayer
    val currentLocation = player.getLocation

    val teleportEffect = for {
      currentLocationIsTeleportTarget <- findTeleportLocation.isTeleportTargetLocation(
        currentLocation
      )
      teleportTargetLocation <- ApplicativeExtra.whenAOrElse(currentLocationIsTeleportTarget)(
        findTeleportLocation.findUpperLocation(currentLocation),
        None
      )
      _ <- teleportTargetLocation.traverse { location =>
        Sync[F].delay(player.teleport(location))
      }
    } yield ()

    teleportEffect.toIO.unsafeRunSync()
  }

  @EventHandler
  def onSneak(e: PlayerToggleSneakEvent): Unit = {
    val player = e.getPlayer
    val currentLocation = player.getLocation

    val teleportEffect = for {
      currentLocationIsTeleportTarget <- findTeleportLocation.isTeleportTargetLocation(
        currentLocation
      )
      teleportTargetLocation <- ApplicativeExtra.whenAOrElse(currentLocationIsTeleportTarget)(
        findTeleportLocation.findLowerLocation(currentLocation),
        None
      )
      _ <- teleportTargetLocation.traverse { location =>
        Sync[F].delay(player.teleport(location))
      }
    } yield ()

    teleportEffect.toIO.unsafeRunSync()
  }

}
