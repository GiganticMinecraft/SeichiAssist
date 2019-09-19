package com.github.unchama.targetedeffect.player

import com.github.unchama.targetedeffect.TargetedEffect
import kotlin.coroutines.Continuation
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * プレーヤーの位置で音を鳴らすような[TargetedEffect].
 */
case class FocusedSoundEffect(private val sound: Sound,
                              private val volume: Float,
                              private val pitch: Float) extends TargetedEffect[Player] {
  override def runFor(minecraftObject: Player, continuation: Continuation[Unit]) {
    minecraftObject.playSound(minecraftObject.getLocation, sound, volume, pitch)
  }
}
