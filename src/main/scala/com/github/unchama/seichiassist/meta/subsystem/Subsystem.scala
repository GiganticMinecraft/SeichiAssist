package com.github.unchama.seichiassist.meta.subsystem

import cats.effect.IO
import org.bukkit.command.TabExecutor
import org.bukkit.event.Listener

trait Subsystem {

  val listeners: Seq[Listener]

  val commands: Map[String, TabExecutor]

  val repeatedJobs: List[IO[Nothing]]

}

object Subsystem {

  def apply(listenersToBeRegistered: Seq[Listener],
            commandsToBeRegistered: Map[String, TabExecutor],
            repeatedJobsToBeRegistred: List[IO[Nothing]] = List[IO[Nothing]]()): Subsystem = new Subsystem {
    override val listeners: Seq[Listener] = listenersToBeRegistered
    override val commands: Map[String, TabExecutor] = commandsToBeRegistered
    override val repeatedJobs: List[IO[Nothing]] = repeatedJobsToBeRegistred
  }

}
