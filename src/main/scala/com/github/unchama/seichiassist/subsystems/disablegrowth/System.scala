package com.github.unchama.seichiassist.subsystems.disablegrowth

import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.disablegrowth.bukkit.listeners.DisableGrowthListener
import org.bukkit.event.Listener

object System {
  def wired[F[_]]: Subsystem[F] =
    new Subsystem[F] {
      override val listeners: Seq[Listener] = Seq(new DisableGrowthListener())
    }
}
