package com.github.unchama.seichiassist.subsystems.elevator.bukkit.actions

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.elevator.application.actions.FindTeleportLocation
import org.bukkit.Location
import org.bukkit.Material

class BukkitFindTeleportLocation[F[_]: Sync] extends FindTeleportLocation[F, Location] {

  override def currentLocationTeleportFromAsCorrectIs(currentLocation: Location): F[Boolean] =
    Sync[F].delay {
      val isIronBlockBelow =
        currentLocation.clone().add(0, -1, 0).getBlock.getType == Material.IRON_BLOCK
      val isHeavyWeightedPressurePlate =
        currentLocation.getBlock.getType == Material.HEAVY_WEIGHTED_PRESSURE_PLATE

      isIronBlockBelow && isHeavyWeightedPressurePlate
    }

  override def isTeleportTargetLocation(targetLocation: Location): F[Boolean] = Sync[F].delay {
    val isIronBlockBelow =
      targetLocation.clone().add(0, -1, 0).getBlock.getType == Material.IRON_BLOCK
    val isHeavyWeightedPressurePlate =
      targetLocation.getBlock.getType == Material.HEAVY_WEIGHTED_PRESSURE_PLATE
    val isPassableBlockAbove = targetLocation.clone().add(0, 1, 0).getBlock.isPassable

    isIronBlockBelow && isHeavyWeightedPressurePlate && isPassableBlockAbove
  }

  import cats.implicits._

  override def findUpperLocation(currentLocation: Location): F[Option[Location]] = {
    (currentLocation.getY.toInt + 1 until currentLocation.getWorld.getMaxHeight)
      .toVector
      .map { y =>
        val location = currentLocation.clone()
        location.setY(y)

        location
      }
      .findM(isTeleportTargetLocation)
  }

  override def findLowerLocation(currentLocation: Location): F[Option[Location]] = {
    (currentLocation.getY.toInt - 1 until currentLocation.getWorld.getMinHeight by -1)
      .toVector
      .map { y =>
        val location = currentLocation.clone()
        location.setY(y)

        location
      }
      .findM(isTeleportTargetLocation)
  }
}
