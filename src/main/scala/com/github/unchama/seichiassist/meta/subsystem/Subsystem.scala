package com.github.unchama.seichiassist.meta.subsystem

import org.bukkit.command.TabExecutor
import org.bukkit.event.Listener

trait Subsystem {

  val listeners: Seq[Listener]

  val commands: Map[String, TabExecutor]

}

object Subsystem {

  def apply(listenersToBeRegistered: Seq[Listener],
            commandsToBeRegistered: Map[String, TabExecutor]): Subsystem = new Subsystem {
    override val listeners: Seq[Listener] = listenersToBeRegistered
    override val commands: Map[String, TabExecutor] = commandsToBeRegistered
  }

}
