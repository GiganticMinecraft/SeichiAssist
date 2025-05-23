package com.github.unchama.seichiassist.subsystems.gridregion.bukkit

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gridregion.domain.CardinalDirection._
import com.github.unchama.seichiassist.subsystems.gridregion.domain.HorizontalAxisAlignedSubjectiveDirection.Ahead
import com.github.unchama.seichiassist.subsystems.gridregion.domain._
import com.github.unchama.util.external.{WorldEditWrapper, WorldGuardWrapper}
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import org.bukkit.Location
import org.bukkit.entity.Player

class BukkitRegionOperations[F[_]: Sync](
  implicit regionCountAllUntilNowRepository: KeyedDataRepository[Player, Ref[F, RegionCount]]
) extends RegionOperations[F, Location, Player] {
  override def getSelectionCorners(
    currentLocation: Location,
    shape: SubjectiveRegionShape
  ): RegionSelectionCorners[Location] = {
    import scala.util.chaining._

    val lengthsAlongCardinalDirections = shape.lengthsAlongCardinalDirections {
      CardinalDirection.relativeToCardinalDirections(currentLocation.getYaw)(Ahead)
    }

    def nearestGridStartCoordinate(component: Int): Int = Math.floorDiv(component, 15) * 15

    /*
     * startPosition - 北西 (-X, -Z)
     * endPosition - 南東 (+X, +Z)
     * に合わせる。
     */
    val northWestCornerOfRegionUnitContainingCurrentLocation = currentLocation
      .clone()
      .tap(_.setX(nearestGridStartCoordinate(currentLocation.getBlockX)))
      .tap(_.setZ(nearestGridStartCoordinate(currentLocation.getBlockZ)))

    val southEastCornerOfRegionUnitContainingCurrentLocation =
      northWestCornerOfRegionUnitContainingCurrentLocation.clone().tap(_.add(14, 0, 14))

    /*
     * Minecraft での東西南北はそれぞれ +X, -X, +Z, -Z に対応する。
     * したがって、 RegionSelectionCorners の
     * - startPosition は northWestCornerOfRegionUnitContainingCurrentLocation を West, North 方向に、
     * - endPosition は southEastCornerOfRegionUnitContainingCurrentLocation を East, South 方向に、
     * lengthsAlongCardinalDirections が指定する RUL 分だけずらす。
     */
    RegionSelectionCorners(
      northWestCornerOfRegionUnitContainingCurrentLocation
        .clone()
        .tap(
          _.add(
            -lengthsAlongCardinalDirections(West).toMeters,
            0,
            -lengthsAlongCardinalDirections(North).toMeters
          )
        ),
      southEastCornerOfRegionUnitContainingCurrentLocation
        .clone()
        .tap(
          _.add(
            lengthsAlongCardinalDirections(East).toMeters,
            0,
            lengthsAlongCardinalDirections(South).toMeters
          )
        )
    )
  }

  import cats.implicits._

  override def tryCreatingSelectedWorldGuardRegion(player: Player): F[Unit] = for {
    regionCount <- regionCountAllUntilNowRepository(player).get
    regionName = s"${player.getName}_${regionCount.value}"
    selectedProtectedCuboidRegion <- Sync[F].delay {
      WorldEditWrapper.getSelectedRegion(player).map { region =>
        new ProtectedCuboidRegion(
          regionName,
          region.getMinimumPoint.withY(-64),
          region.getMaximumPoint.withY(320)
        )
      }
    }
    wgManager = WorldGuardWrapper.getRegionManager(player.getWorld)
    regionCreateResult <- Sync[F].delay {
      selectedProtectedCuboidRegion.foreach(wgManager.addRegion)
    }
    _ <- Sync[F].delay {
      selectedProtectedCuboidRegion.foreach(protectedCuboidRegion =>
        WorldGuardWrapper.addRegionOwner(protectedCuboidRegion, player)
      )
    }
    _ <- regionCountAllUntilNowRepository(player).update(_.increment)
  } yield regionCreateResult

  override def canCreateRegion(
    player: Player,
    shape: SubjectiveRegionShape
  ): F[RegionCreationResult] = {
    for {
      world <- Sync[F].delay(player.getWorld)
      wgManager = WorldGuardWrapper.getRegionManager(world)
      isGridProtectionEnabled <- Sync[F].delay(
        SeichiAssist.seichiAssistConfig.isGridProtectionEnabled(world)
      )
      worldEditSelection <- Sync[F].delay(WorldEditWrapper.getSelection(player))
      applicableRegions <- Sync[F].delay(wgManager.getApplicableRegions(worldEditSelection))
      regionCountPerPlayer <- Sync[F].delay(WorldGuardWrapper.getNumberOfRegions(player, world))
      maxRegionCountPerWorld <- Sync[F].delay(WorldGuardWrapper.getWorldMaxRegion(world))
    } yield {
      if (!isGridProtectionEnabled) {
        RegionCreationResult.WorldProhibitsRegionCreation
      } else if (
        regionCountPerPlayer < maxRegionCountPerWorld && applicableRegions.size() == 0
      ) {
        RegionCreationResult.Success
      } else {
        RegionCreationResult.Error
      }
    }

  }
}
