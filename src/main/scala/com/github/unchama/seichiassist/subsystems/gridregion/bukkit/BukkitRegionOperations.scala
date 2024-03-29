package com.github.unchama.seichiassist.subsystems.gridregion.bukkit

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gridregion.domain.CardinalDirection._
import com.github.unchama.seichiassist.subsystems.gridregion.domain.HorizontalAxisAlignedSubjectiveDirection.Ahead
import com.github.unchama.seichiassist.subsystems.gridregion.domain._
import com.github.unchama.util.external.{WorldEditWrapper, WorldGuardWrapper}
import org.bukkit.Location
import org.bukkit.entity.Player

class BukkitRegionOperations[F[_]: Sync](
  implicit regionCountRepository: KeyedDataRepository[Player, Ref[F, RegionCount]]
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
    regionCount <- regionCountRepository(player).get
    regionCreateResult <- Sync[F].delay {
      WorldEditWrapper
        .getSelection(player)
        .map { selection =>
          val regionName = s"${player.getName}_${regionCount.value}"

          WorldGuardWrapper.tryCreateRegion(
            regionName,
            player,
            player.getWorld,
            selection.getNativeMinimumPoint.toBlockVector,
            selection.getNativeMaximumPoint.toBlockVector
          )
        }
        .getOrElse(())
    }
    _ <- regionCountRepository(player).update(_.increment)
  } yield regionCreateResult

  override def canCreateRegion(
    player: Player,
    shape: SubjectiveRegionShape
  ): F[RegionCreationResult] = {
    val selection = WorldEditWrapper.getSelection(player)
    for {
      regionCount <- regionCountRepository(player).get
      world <- Sync[F].delay(player.getWorld)
      wgManager = WorldGuardWrapper.getRegionManager(world)
      result <-
        if (!SeichiAssist.seichiAssistConfig.isGridProtectionEnabled(world)) {
          Sync[F].pure(RegionCreationResult.WorldProhibitsRegionCreation)
        } else if (selection.isEmpty || wgManager.isEmpty) {
          Sync[F].pure(RegionCreationResult.Error)
        } else {
          Sync[F].delay {
            val regions = WorldGuardWrapper.getApplicableRegionCount(
              world,
              s"${player.getName}_${regionCount.value}",
              selection.get.getNativeMinimumPoint.toBlockVector,
              selection.get.getNativeMaximumPoint.toBlockVector
            )
            if (regions != 0) {
              RegionCreationResult.Error
            } else {
              val maxRegionCount = WorldGuardWrapper.getMaxRegionCount(player, world)
              val regionCountPerPlayer = WorldGuardWrapper.getRegionCountOfPlayer(player, world)

              if (maxRegionCount >= 0 && regionCountPerPlayer >= maxRegionCount) {
                RegionCreationResult.Error
              } else {
                RegionCreationResult.Success
              }
            }
          }
        }
    } yield result

  }
}
