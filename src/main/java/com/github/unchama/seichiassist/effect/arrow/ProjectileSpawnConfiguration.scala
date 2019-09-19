package com.github.unchama.seichiassist.effect.arrow

case class ProjectileSpawnConfiguration(
    val speed: Double,
    private val offsetComponents: Triple<Double, Double, Double> = Triple(0.0, 0.0, 0.0),
    val gravity: Boolean = false
) {
  val offset: Vector
    get() = offsetComponents.let { (x, y, z) -> Vector(x, y, z) }
}
