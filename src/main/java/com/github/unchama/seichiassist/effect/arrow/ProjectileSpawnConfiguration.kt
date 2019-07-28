package com.github.unchama.seichiassist.effect.arrow

import org.bukkit.util.Vector

data class ProjectileSpawnConfiguration(
    val speed: Double,
    private val offsetComponents: Triple<Double, Double, Double> = Triple(0.0, 0.0, 0.0),
    val gravity: Boolean = false
) {
  val offset: Vector
    get() = offsetComponents.let { (x, y, z) -> Vector(x, y, z) }
}
