package com.github.unchama.seichiassist.effect.arrow

import org.bukkit.util.Vector

case class ProjectileSpawnConfiguration(
    val speed: Double,
    private val offsetComponents: Triple[Double, Double, Double] = Triple(0.0, 0.0, 0.0),
    val gravity: Boolean = false
) {
  val offset: Vector = offsetComponents match { case (x, y, z) => new Vector(x, y, z) }
}
