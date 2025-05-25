package com.github.unchama.seichiassist.subsystems.gridregion.bukkit

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gridregion.domain.CardinalDirection.West
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{
  RegionDefiner,
  RegionSelectionCorners,
  SubjectiveRegionShape,
  CardinalDirection
}
import com.github.unchama.seichiassist.subsystems.gridregion.domain.CardinalDirection._
import com.github.unchama.seichiassist.subsystems.gridregion.domain.HorizontalAxisAlignedSubjectiveDirection.Ahead
import org.bukkit.Location

class BukkitRegionDefiner[F[_]: Sync] extends RegionDefiner[F, Location] {

  /**
   * @return `currentLocation` から `shape` を使って保護範囲の始点と終点を求める
   */
  override def getSelectionCorners(
    currentLocation: Location,
    shape: SubjectiveRegionShape
  ): F[RegionSelectionCorners[Location]] = Sync[F].delay {
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
}
