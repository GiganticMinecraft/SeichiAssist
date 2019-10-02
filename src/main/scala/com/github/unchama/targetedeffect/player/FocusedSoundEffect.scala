package com.github.unchama.targetedeffect.player

import cats.effect.IO
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * プレーヤーの位置で音を鳴らすような[TargetedEffect].
 */
case class FocusedSoundEffect(sound: Sound, volume: Float, pitch: Float) extends TargetedEffect[Player] {
  override def apply(v1: Player): IO[Unit] = IO {
    v1.playSound(v1.getLocation, sound, volume, pitch)
  }
}
