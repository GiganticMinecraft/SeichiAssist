package com.github.unchama.seichiassist.effects.unfocused

import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.Sound

object BroadcastSoundEffect {
  def apply(sound: Sound, volume: Float, pitch: Float): TargetedEffect[Any] =
    BroadcastEffect(FocusedSoundEffect(sound, volume, pitch))
}
