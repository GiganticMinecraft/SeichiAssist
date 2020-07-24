package com.github.unchama.seichiassist.mebius

import com.github.unchama.seichiassist.SubsystemEntryPoints
import com.github.unchama.seichiassist.mebius.controller.PropertyModificationBukkitMessages
import com.github.unchama.seichiassist.mebius.controller.command.MebiusCommand
import com.github.unchama.seichiassist.mebius.controller.listeners.{MebiusDropTrialListener, MebiusInteractionResponder, MebiusLevelUpTrialListener, MebiusRenamePreventionListener}
import com.github.unchama.seichiassist.mebius.domain.PropertyModificationMessages

object EntryPoints {
  val wired: SubsystemEntryPoints = {
    implicit val messages: PropertyModificationMessages = PropertyModificationBukkitMessages
    val listeners = Seq(
      new MebiusDropTrialListener,
      new MebiusInteractionResponder,
      new MebiusLevelUpTrialListener,
      new MebiusRenamePreventionListener
    )

    val commands = Map(
      "mebius" -> MebiusCommand.executor
    )

    SubsystemEntryPoints(listeners, commands)
  }
}
