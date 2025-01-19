package com.github.unchama.seichiassist.subsystems.joinandquitmessenger

import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.joinandquitmessenger.bukkit.JoinAndQuitListeners
import org.bukkit.event.Listener

object System {

  def wired[F[_]]: Subsystem[F] = {
    new Subsystem[F] {
      override val listeners: Seq[Listener] = Seq(new JoinAndQuitListeners)
    }
  }

}
