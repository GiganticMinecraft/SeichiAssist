package com.github.unchama.seichiassist.subsystems.rescueplayer

import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.rescueplayer.bukkit.listeners.RescuePlayerListener

object System {
  def wired[F[_]]: Subsystem[F] = {
    val listeners = Seq(
      new RescuePlayerListener()
    )

    Subsystem(listeners, Nil, Map())
  }
}
