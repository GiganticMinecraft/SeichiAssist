package com.github.unchama.seichiassist.subsystems.webhook

import com.github.unchama.seichiassist.meta.subsystem.Subsystem

object System {
  def wired[F[_]]: Subsystem[F] = Subsystem(Seq(), Map())
}
