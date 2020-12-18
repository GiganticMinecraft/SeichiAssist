package com.github.unchama.seichiassist.subsystems.loginbonusticket

import com.github.unchama.seichiassist.meta.subsystem.Subsystem

object System {
  def wired[F[_]]: Subsystem[F] = {
    val listeners = Seq.empty

    Subsystem(listeners, Nil, Map())
  }
}