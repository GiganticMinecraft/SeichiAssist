package com.github.unchama.seichiassist.subsystems.bookedachivement

import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.bookedachivement.bukkit.listener.GrantBookedAchievementListener

object System {
  def wired(implicit effectEnvironment: EffectEnvironment): Subsystem = {
    val listener = Seq(
      new GrantBookedAchievementListener()
    )

    Subsystem(listener, Map())
  }
}
