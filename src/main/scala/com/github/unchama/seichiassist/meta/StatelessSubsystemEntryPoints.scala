package com.github.unchama.seichiassist.meta

import org.bukkit.command.TabExecutor
import org.bukkit.event.Listener

case class StatelessSubsystemEntryPoints(listenersToBeRegistered: Seq[Listener],
                                         commandsToBeRegistered: Map[String, TabExecutor])
