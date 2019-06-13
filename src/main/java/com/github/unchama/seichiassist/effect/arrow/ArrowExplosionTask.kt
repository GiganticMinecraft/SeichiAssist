package com.github.unchama.seichiassist.effect.arrow

import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.entity.SmallFireball
import org.bukkit.util.Vector

class ArrowExplosionTask(player: Player) : AbstractEffectTask() {
  override val additionalVector: Vector
    get() = Vector(0.0, 1.6, 0.0)

  override val vectorMultiplier: Double
    get() = 0.4

  init {
    //スキルを実行する処理
    val loc = player.location.clone()
    runEffect<SmallFireball>(loc, player, false, Sound.ENTITY_GHAST_SHOOT)
  }
}
