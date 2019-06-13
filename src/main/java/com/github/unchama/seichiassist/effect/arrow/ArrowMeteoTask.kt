package com.github.unchama.seichiassist.effect.arrow

import org.bukkit.Sound
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class ArrowMeteoTask(player: Player) : AbstractEffectTask() {
  override val additionalVector: Vector
    get() = Vector(0.0, 1.6, 0.0)

  override val vectorMultiplier: Double
    get() = 1.0

  init {
    //スキルを実行する処理
    val loc = player.location.clone()
    runEffect<Arrow>(loc, player, false, Sound.ENTITY_ARROW_SHOOT) {
      isGlowing = true
    }
  }
}
