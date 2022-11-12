package com.github.unchama.seichiassist.subsystems.gridregion.bukkit

import com.github.unchama.seichiassist.subsystems.gridregion.domain.{
  Direction,
  RegionOperations,
  RegionSelection,
  RegionUnits
}
import org.bukkit.Location

class BukkitRegionOperations extends RegionOperations[Location] {

  override def getSelection(
    currentLocation: Location,
    regionUnits: RegionUnits,
    direction: Direction
  ): RegionSelection[Location] = {

    /*
     * startPosition - 北西
     * endPosition - 南東
     * に合わせる
     */
    val (startPosition, endPosition) = direction match {
      case Direction.East =>
        (
          currentLocation.subtract(
            regionUnits.behind.unitPerBlockAmount,
            0.0,
            regionUnits.left.unitPerBlockAmount
          ),
          currentLocation.add(
            regionUnits.ahead.unitPerBlockAmount,
            0.0,
            regionUnits.right.unitPerBlockAmount
          )
        )
      case Direction.North =>
        (
          currentLocation.subtract(
            regionUnits.left.unitPerBlockAmount,
            0.0,
            regionUnits.ahead.unitPerBlockAmount
          ),
          currentLocation.add(
            regionUnits.right.unitPerBlockAmount,
            0.0,
            regionUnits.behind.unitPerBlockAmount
          )
        )
      case Direction.South =>
        (
          currentLocation.subtract(
            regionUnits.right.unitPerBlockAmount,
            0.0,
            regionUnits.behind.unitPerBlockAmount
          ),
          currentLocation.add(
            regionUnits.left.unitPerBlockAmount,
            0.0,
            regionUnits.ahead.unitPerBlockAmount
          )
        )
      case Direction.West =>
        (
          currentLocation.subtract(
            regionUnits.ahead.unitPerBlockAmount,
            0.0,
            regionUnits.right.unitPerBlockAmount
          ),
          currentLocation.add(
            regionUnits.behind.unitPerBlockAmount,
            0.0,
            regionUnits.left.unitPerBlockAmount
          )
        )
    }

    RegionSelection(startPosition, endPosition)
  }

}
