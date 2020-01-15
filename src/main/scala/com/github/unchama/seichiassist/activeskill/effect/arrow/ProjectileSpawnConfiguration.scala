package com.github.unchama.seichiassist.activeskill.effect.arrow

import org.bukkit.util.Vector

case class ProjectileSpawnConfiguration(speed: Double,
                                        private val offsetComponents: (Double, Double, Double) = (0.0, 0.0, 0.0),
                                        gravity: Boolean = false
                                       ) {
  val offset: Vector = offsetComponents match {
    case (x, y, z) => new Vector(x, y, z)
  }
}
