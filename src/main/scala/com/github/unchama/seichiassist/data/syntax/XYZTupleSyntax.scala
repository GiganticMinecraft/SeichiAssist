package com.github.unchama.seichiassist.data.syntax

import com.github.unchama.seichiassist.data.XYZTuple
import org.bukkit.{Location, World}

trait XYZTupleSyntax {
  implicit class XYZTupleIntOps(k: Int) {
    def *(tuple: XYZTuple): XYZTuple = tuple.mapEachComponent(_ * k)
  }

  implicit class XYZTupleOps(a: XYZTuple) {
    def mapEachComponent(f: Int => Int): XYZTuple = XYZTuple(f(a.x), f(a.y), f(a.z))

    def +(another: XYZTuple) = XYZTuple(a.x + another.x, a.y + another.y, a.z + another.z)

    def negative: XYZTuple = (-1) * a

    def -(another: XYZTuple): XYZTuple = a + another.negative

    def toLocation(world: World): Location = new Location(world, a.x.toDouble, a.y.toDouble, a.z.toDouble)

    def /(k: Double): XYZTuple = mapEachComponent(c => (c / k).toInt)

    def toBukkitVector = new org.bukkit.util.Vector(a.x, a.y, a.z)
  }

  implicit class RichLocation(location: Location) {
    def +(vector: XYZTuple): Location = location.clone().add(vector.toBukkitVector)
  }
}

object XYZTupleSyntax extends XYZTupleSyntax