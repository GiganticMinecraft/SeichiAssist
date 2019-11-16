package com.github.unchama.seichiassist.effect

import com.github.unchama.seichiassist.data.Coordinate

case class XYZTuple(x: Int, y: Int, z: Int)

object XYZTuple {

  implicit class CoordinateOps(val coordinate: Coordinate) extends AnyVal {
  }

  case class AxisAlignedCuboid(begin: XYZTuple, end: XYZTuple)

  implicit class AACOps(val cuboid: AxisAlignedCuboid) extends AnyVal {

    import cuboid._

    def forEachGridPoint(gridWidth: Int = 1)(action: XYZTuple => Unit): Unit = {
      Range.inclusive(begin.x, end.x, gridWidth).foreach { x =>
        Range.inclusive(begin.y, end.y, gridWidth).foreach { y =>
          Range.inclusive(begin.z, end.z, gridWidth).foreach { z =>
            action(XYZTuple(x, y, z))
          }
        }
      }
    }
  }

}
