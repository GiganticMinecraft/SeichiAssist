package com.github.unchama.seichiassist.effect

case class XYZTuple(val x: Int, val y: Int, val z: Int)

object XYZTuple {
  def Coordinate.toXYZTuple() = XYZTuple(x, y, z)

  case class AxisAlignedCuboid(val begin: XYZTuple, val end: XYZTuple)

  def AxisAlignedCuboid.forEachGridPoint(gridWidth: Int = 1, action: (XYZTuple) => Unit) {
    (begin.x .. end.x).step(gridWidth).forEach { x =>
      (begin.y .. end.y).step(gridWidth).forEach { y =>
        (begin.z .. end.z).step(gridWidth).forEach { z =>
          action(XYZTuple(x, y, z))
        }
      }
    }
  }
}
