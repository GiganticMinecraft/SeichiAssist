package com.github.unchama.seichiassist.meta.subsystem

import org.bukkit.command.TabExecutor
import org.bukkit.event.Listener

trait StatefulSubsystem[S] extends Subsystem {

  val state: S

}

object StatefulSubsystem {

  def apply[S](listenersToBeRegistered: Seq[Listener],
               commandsToBeRegistered: Map[String, TabExecutor],
               stateToExpose: S): StatefulSubsystem[S] = new StatefulSubsystem[S] {
    override val state: S = stateToExpose
    override val listeners: Seq[Listener] = listenersToBeRegistered
    override val commands: Map[String, TabExecutor] = commandsToBeRegistered
  }

}
