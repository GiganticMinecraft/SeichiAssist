package com.github.unchama.seichiassist.effect


case class XYZTuple(x: Int, y: Int, z: Int)

object XYZTuple {
  case class AxisAlignedCuboid(begin: XYZTuple, end: XYZTuple)

  implicit class AACOps(val cuboid: AxisAlignedCuboid) extends AnyVal {

    import cuboid._

    def forEachGridPoint(gridWidth: Int = 1)(action: XYZTuple => Unit): Unit = {
      def sort(a: Int, b: Int) = if (a < b) (a, b) else (b, a)

      val (xSmall, xLarge) = sort(begin.x, end.x)
      val (ySmall, yLarge) = sort(begin.y, end.y)
      val (zSmall, zLarge) = sort(begin.z, end.z)

      Range.inclusive(xSmall, xLarge, gridWidth).foreach { x =>
        Range.inclusive(ySmall, yLarge).foreach { y =>
          Range.inclusive(zSmall, zLarge).foreach { z =>
            action(XYZTuple(x, y, z))
          }
        }
      }
    }
  }

}
