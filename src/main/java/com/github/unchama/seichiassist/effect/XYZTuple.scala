package com.github.unchama.seichiassist.effect

import com.github.unchama.seichiassist.data.Coordinate

case class XYZTuple(val x: Int, val y: Int, val z: Int)

object XYZTuple {
  implicit class CoordinateOps(val coordinate: Coordinate) extends AnyVal {
    import coordinate._

    def toXYZTuple() = XYZTuple(x, y, z)
  }

  case class AxisAlignedCuboid(val begin: XYZTuple, val end: XYZTuple)

  implicit case class AACOps(cuboid: AxisAlignedCuboid) extends AnyVal {
    import cuboid._

    def forEachGridPoint(gridWidth: Int = 1, action: (XYZTuple) => Unit) {
      (begin.x .. end.x).step(gridWidth).forEach { x =>
        (begin.y .. end.y).step(gridWidth).forEach { y =>
          (begin.z .. end.z).step(gridWidth).forEach { z =>
            action(XYZTuple(x, y, z))
          }
        }
      }
    }
  }
}
