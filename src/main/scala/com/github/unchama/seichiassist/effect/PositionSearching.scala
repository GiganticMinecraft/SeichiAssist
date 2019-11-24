package com.github.unchama.seichiassist.effect

import com.github.unchama.seichiassist.effect.XYZTuple.AxisAlignedCuboid
import org.bukkit.Location
import org.bukkit.block.Block

object PositionSearching {
  /**
   * [location]を中心としたチェビシェフ距離が[distance]以下の領域に
   * [matchAgainst]の[[XYZTuple]]が一つでも含まれているかを返す。
   */
  def containsOneOfPositionsAround(center: XYZTuple, distance: Int, matchAgainst: Set[XYZTuple]): Boolean = {
    import XYZTupleSyntax._

    val sphereVertex = XYZTuple(distance, distance, distance)
    val cuboidToLookFor = AxisAlignedCuboid(sphereVertex.negative, sphereVertex)

    cuboidToLookFor.forEachGridPoint() { vector =>
      if (matchAgainst.contains(center + vector)) return true
    }

    false
  }
}
