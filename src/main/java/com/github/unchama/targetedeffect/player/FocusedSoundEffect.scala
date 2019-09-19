package com.github.unchama.targetedeffect.player

import org.bukkit.Sound

/**
 * プレーヤーの位置で音を鳴らすような[TargetedEffect].
 */
case class FocusedSoundEffect(private val sound: Sound,
                              private val volume: Float,
                              private val pitch: Float): TargetedEffect<Player> {
  override suspend def runFor(minecraftObject: Player) {
    minecraftObject.playSound(minecraftObject.location, sound, volume, pitch)
  }
}
