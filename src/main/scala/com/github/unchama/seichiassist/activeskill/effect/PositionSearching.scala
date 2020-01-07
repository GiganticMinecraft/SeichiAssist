package com.github.unchama.seichiassist.activeskill.effect

import com.github.unchama.seichiassist.data.{AxisAlignedCuboid, XYZTuple}

object PositionSearching {
  /**
   * [location]を中心としたチェビシェフ距離が[distance]以下の領域に
   * [matchAgainst]の[[XYZTuple]]が一つでも含まれているかを返す。
   */
  def containsOneOfPositionsAround(center: XYZTuple, distance: Int, matchAgainst: Set[XYZTuple]): Boolean = {
    import com.github.unchama.seichiassist.data.syntax._

    val sphereVertex = XYZTuple(distance, distance, distance)
    val cuboidToLookFor = AxisAlignedCuboid(sphereVertex.negative, sphereVertex)

    cuboidToLookFor.gridPoints()
      .map(center + _)
      .exists(matchAgainst.contains)
  }
}
