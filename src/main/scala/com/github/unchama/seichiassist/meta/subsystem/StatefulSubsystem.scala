package com.github.unchama.seichiassist.meta.subsystem

import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener

trait StatefulSubsystem[F[_], S] extends Subsystem[F] {

  val state: S

}

object StatefulSubsystem {

  def apply[F[_], S](listenersToBeRegistered: Seq[Listener],
                     finalizersToBeManaged: Seq[PlayerDataFinalizer[F, Player]],
                     commandsToBeRegistered: Map[String, TabExecutor],
                     stateToExpose: S): StatefulSubsystem[F, S] = new StatefulSubsystem[F, S] {
    override val state: S = stateToExpose
    override val listeners: Seq[Listener] = listenersToBeRegistered
    override val commands: Map[String, TabExecutor] = commandsToBeRegistered
    override val managedFinalizers: Seq[PlayerDataFinalizer[F, Player]] = finalizersToBeManaged
  }

}
