package com.github.unchama.seichiassist.meta.subsystem

import cats.~>
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.generic.ContextCoercion
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener

trait StatefulSubsystem[F[_], S] extends Subsystem[F] {

  val state: S

  override def transformFinalizationContext[G[_]](trans: F ~> G): StatefulSubsystem[G, S] =
    StatefulSubsystem.withState(super.transformFinalizationContext(trans), state)

  override def coerceFinalizationContextTo[G[_] : ContextCoercion[F, *[_]]]: StatefulSubsystem[G, S] = transformFinalizationContext(implicitly)

}

object StatefulSubsystem {

  def withState[F[_], S](system: Subsystem[F], _state: S): StatefulSubsystem[F, S] = {
    new StatefulSubsystem[F, S] {
      override val state: S = _state
      override val listeners: Seq[Listener] = system.listeners
      override val managedFinalizers: Seq[PlayerDataFinalizer[F, Player]] = system.managedFinalizers
      override val commands: Map[String, TabExecutor] = system.commands
    }
  }

  def apply[F[_], S](listenersToBeRegistered: Seq[Listener],
                     finalizersToBeManaged: Seq[PlayerDataFinalizer[F, Player]],
                     commandsToBeRegistered: Map[String, TabExecutor],
                     stateToExpose: S): StatefulSubsystem[F, S] = {
    val subsystem = Subsystem(listenersToBeRegistered, finalizersToBeManaged, commandsToBeRegistered)
    withState(subsystem, stateToExpose)
  }

}
