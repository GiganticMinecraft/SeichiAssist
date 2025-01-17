package com.github.unchama.seichiassist.subsystems.canceldamagebyfallingblocks

import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.canceldamagebyfallingblocks.bukkit.listeners.PlayerDamageByBlockEvents
import org.bukkit.event.Listener

object System {

  def wired[F[_]]: Subsystem[F] = {
    new Subsystem[F] {
      override val listeners: Seq[Listener] = Seq(PlayerDamageByBlockEvents)
    }
  }

}
