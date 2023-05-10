package com.github.unchama.targetedeffect.player

import cats.effect.Sync
import com.github.unchama.targetedeffect.{TargetedEffect, TargetedEffectF}
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * プレーヤーの位置で音を鳴らすような[TargetedEffect].
 */
object FocusedSoundEffect {
  def apply(sound: Sound, volume: Float, pitch: Float): TargetedEffect[Player] =
    TargetedEffect.delay { player =>
      // 音を鳴らすのは非同期スレッドでも問題ない(Spigot 1.12.2)
      player.playSound(player.getLocation, sound, volume, pitch)
    }
}

object FocusedSoundEffectF {
  def apply[F[_]: Sync](sound: Sound, volume: Float, pitch: Float): TargetedEffectF[F, Player] =
    TargetedEffect.delay[F, Player] { player =>
      // 音を鳴らすのは非同期スレッドでも問題ない(Spigot 1.12.2)
      player.playSound(player.getLocation, sound, volume, pitch)
    }
}
