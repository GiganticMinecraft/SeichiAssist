package com.github.unchama.seichiassist.data

import org.bukkit.Location

case class XYZTuple(x: Int, y: Int, z: Int)

object XYZTuple {
  def of(location: Location): XYZTuple =
    XYZTuple(location.getBlockX, location.getBlockY, location.getBlockZ)
}

case class AxisAlignedCuboid(begin: XYZTuple, end: XYZTuple)
