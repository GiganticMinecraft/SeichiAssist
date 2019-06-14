package com.github.unchama.seichiassist.effect.arrow

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.effect.FixedMetadataValueHolder
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

abstract class AbstractEffectTask : BukkitRunnable() {
  abstract val additionalVector: Vector

  abstract val vectorMultiplier: Double

  override fun run() {
    // nop
  }

  inline fun <reified P : Projectile> runEffect(loc: Location, shoot: Player, gravity: Boolean = false, sound: Sound? = null, modifyProjectile: P.() -> Unit = {}) {
    if (sound != null) {
      shoot.playSound(shoot.location, sound, 1f, 1.3f)
    }

    val proj = loc.world.spawn(loc, P::class.java).apply {
      shooter = shoot
      setGravity(gravity)
    }
    proj.apply(modifyProjectile)
    val vec = shoot.location.clone().add(loc.direction).add(additionalVector).direction.multiply(vectorMultiplier)

    // launch
    SeichiAssist.entitylist += proj
    proj.setMetadata("ArrowSkill", FixedMetadataValueHolder.TRUE)
    proj.velocity = vec

    Bukkit.getScheduler().schedule(SeichiAssist.instance, SynchronizationContext.ASYNC) {
      waitFor(100)
      proj.remove()
      SeichiAssist.entitylist.remove(proj)
    }
  }
}
