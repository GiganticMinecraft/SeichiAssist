package com.github.unchama.seichiassist.subsystems.rescueplayer

import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.rescueplayer.bukkit.listeners.RescuePlayerListener
import org.bukkit.event.Listener

object System {
  def wired[F[_]]: Subsystem[F] =
    new Subsystem[F] {
      override val listeners: Seq[Listener] = Seq(new RescuePlayerListener())
    }
}
