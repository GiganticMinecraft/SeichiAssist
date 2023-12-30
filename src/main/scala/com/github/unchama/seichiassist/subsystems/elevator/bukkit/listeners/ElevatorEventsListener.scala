package com.github.unchama.seichiassist.subsystems.elevator.bukkit.listeners

import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.generic.ApplicativeExtra
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.subsystems.elevator.application.actions.FindTeleportLocation
import org.bukkit.Location
import org.bukkit.event.player.{PlayerMoveEvent, PlayerToggleSneakEvent}
import org.bukkit.event.{EventHandler, Listener}

class ElevatorEventsListener[F[_]: ConcurrentEffect](
  implicit findTeleportLocation: FindTeleportLocation[F, Location],
  effectEnvironment: EffectEnvironment
) extends Listener {

  import cats.implicits._

  @EventHandler
  def onJump(e: PlayerMoveEvent): Unit = {
    val player = e.getPlayer
    val currentLocation = player.getLocation

    if (player.isFlying || e.getFrom.getY >= e.getTo.getY) return

    val teleportEffect = for {
      currentLocationIsCorrectTeleportLocation <- findTeleportLocation
        .currentLocationTeleportFromAsCorrectIs(currentLocation)
      teleportTargetLocation <- ApplicativeExtra.whenAOrElse(
        currentLocationIsCorrectTeleportLocation
      )(findTeleportLocation.findUpperLocation(currentLocation), None)
      _ <- teleportTargetLocation.traverse { location =>
        Sync[F].delay(player.teleport(location))
      }
    } yield ()

    effectEnvironment.unsafeRunEffectAsync("エレベータの上昇処理を行う", teleportEffect)
  }

  @EventHandler
  def onSneak(e: PlayerToggleSneakEvent): Unit = {
    val player = e.getPlayer
    val currentLocation = player.getLocation

    if (!player.isSneaking) return

    val teleportEffect = for {
      currentLocationIsCorrectTeleportLocation <- findTeleportLocation
        .currentLocationTeleportFromAsCorrectIs(currentLocation)
      teleportTargetLocation <- ApplicativeExtra.whenAOrElse(
        currentLocationIsCorrectTeleportLocation
      )(findTeleportLocation.findLowerLocation(currentLocation), None)
      _ <- teleportTargetLocation.traverse { location =>
        Sync[F].delay(player.teleport(location))
      }
    } yield ()

    effectEnvironment.unsafeRunEffectAsync("エレベータの降下処理を行う", teleportEffect)
  }

}
