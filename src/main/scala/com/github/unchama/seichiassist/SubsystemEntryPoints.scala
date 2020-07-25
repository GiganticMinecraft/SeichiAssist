package com.github.unchama.seichiassist

import org.bukkit.command.TabExecutor
import org.bukkit.event.Listener

case class SubsystemEntryPoints(listenersToBeRegistered: Seq[Listener],
                                commandsToBeRegistered: Map[String, TabExecutor])
