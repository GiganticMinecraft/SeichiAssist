package com.github.unchama.targetedeffect.player

import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * プレーヤーの位置で音を鳴らすような[TargetedEffect].
 */
object FocusedSoundEffect {
  def apply(sound: Sound, volume: Float, pitch: Float): TargetedEffect[Player] =
    targetedeffect.delay { player =>
      // 音を鳴らすのは非同期スレッドでも問題ない(Spigot 1.12.2)
      player.playSound(player.getLocation, sound, volume, pitch)
    }
}
