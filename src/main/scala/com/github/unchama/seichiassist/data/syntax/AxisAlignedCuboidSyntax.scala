package com.github.unchama.seichiassist.data.syntax

import com.github.unchama.seichiassist.data.{AxisAlignedCuboid, XYZTuple}

trait AxisAlignedCuboidSyntax {
  implicit class AxisAlignedCuboidOps(val cuboid: AxisAlignedCuboid) {
    import cuboid._

    def gridPoints(gridWidth: Int = 1): IndexedSeq[XYZTuple] = {
      def sort(a: Int, b: Int): (Int, Int) = if (a < b) (a, b) else (b, a)

      val (xSmall, xLarge) = sort(begin.x, end.x)
      val (ySmall, yLarge) = sort(begin.y, end.y)
      val (zSmall, zLarge) = sort(begin.z, end.z)

      val xRange = Range.inclusive(xSmall, xLarge, gridWidth)
      val yRange = Range.inclusive(ySmall, yLarge, gridWidth)
      val zRange = Range.inclusive(zSmall, zLarge, gridWidth)

      for { x <- xRange; y <- yRange; z <- zRange } yield XYZTuple(x, y, z)
    }
  }
}
