package com.github.unchama.targetedeffect.player

import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * プレーヤーの位置で音を鳴らすような[TargetedEffect].
 */
data class FocusedSoundEffect(private val sound: Sound,
                              private val volume: Float,
                              private val pitch: Float): TargetedEffect<Player> {
  override suspend fun runFor(minecraftObject: Player) {
    minecraftObject.playSound(minecraftObject.location, sound, volume, pitch)
  }
}
