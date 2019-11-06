package com.github.unchama.seichiassist

import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.Sound
import org.bukkit.entity.Player

object CommonSoundEffects {
  val menuTransitionFenceSound: TargetedEffect[Player] =
    FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f)
}