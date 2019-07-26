package com.github.unchama.seichiassist.effect

import com.github.unchama.seichiassist.data.Coordinate

data class XYZTuple(val x: Int, val y: Int, val z: Int)

fun Coordinate.toXYZTuple() = XYZTuple(x, y, z)

data class AxisAlignedCuboid(val begin: XYZTuple, val end: XYZTuple)

inline fun AxisAlignedCuboid.forEachGridPoint(gridWidth: Int = 1, action: (XYZTuple) -> Unit) {
  (begin.x .. end.x).step(gridWidth).forEach { x ->
    (begin.y .. end.y).step(gridWidth).forEach { y ->
      (begin.z .. end.z).step(gridWidth).forEach { z ->
        action(XYZTuple(x, y, z))
      }
    }
  }
}
