package com.github.unchama.seichiassist.subsystems.managedfly

import com.github.unchama.seichiassist.meta.subsystem.Subsystem

/**
 * NOTE: このサブシステム(managedfly)は本来BuildAssist側に属するが、
 * BuildAssistがSeichiAssistのサブシステムとして完全に整理されなおすまでは、
 * SeichiAssist直属のサブシステムとして扱う。
 */
object System {
  def wired: Subsystem = {
    Subsystem(
      listenersToBeRegistered = Seq(),
      commandsToBeRegistered = Map()
    )
  }
}
