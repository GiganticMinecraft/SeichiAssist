package com.github.unchama.seichiassist.subsystems.managedfly

import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.seichiassist.meta.subsystem.Subsystem

/**
 * NOTE: このサブシステム(managedfly)は本来BuildAssist側に属するが、
 * BuildAssistがSeichiAssistのサブシステムとして完全に整理されなおすまでは、
 * SeichiAssist直属のサブシステムとして扱う。
 */
object System {

  import cats.implicits._

  def wired[F[_] : ConcurrentEffect, G[_] : SyncEffect]: F[Subsystem] = {
    Subsystem(
      listenersToBeRegistered = Seq(),
      commandsToBeRegistered = Map()
    ).pure[F]
  }
}
