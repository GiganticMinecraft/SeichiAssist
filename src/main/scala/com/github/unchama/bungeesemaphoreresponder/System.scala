package com.github.unchama.bungeesemaphoreresponder

import akka.actor.ActorSystem
import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import com.github.unchama.bungeesemaphoreresponder.bukkit.listeners.BungeeSemaphoreCooperator
import com.github.unchama.bungeesemaphoreresponder.domain.actions.BungeeSemaphoreSynchronization
import com.github.unchama.bungeesemaphoreresponder.domain.{PlayerDataFinalizer, PlayerName}
import com.github.unchama.bungeesemaphoreresponder.infrastructure.redis.RedisBungeeSemaphoreSynchronization
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.entity.Player
import org.bukkit.event.Listener

class System[F[_]: ConcurrentEffect: Timer](
  val finalizers: List[PlayerDataFinalizer[F, Player]],
  messagePublishingContext: ContextShift[IO]
)(
  implicit configuration: Configuration,
  _akkaSystem: ActorSystem,
  effectEnvironment: EffectEnvironment
) {
  // We wish to be more explicit on the context shift that will be used within this system,
  // so we don't receive it as an implicit parameter
  implicit val _contextShift: ContextShift[IO] = messagePublishingContext

  val listenersToBeRegistered: Seq[Listener] = {
    implicit val _synchronization: BungeeSemaphoreSynchronization[F[Unit], PlayerName] = {
      new RedisBungeeSemaphoreSynchronization[F]()
    }
    Seq(new BungeeSemaphoreCooperator[F](finalizers))
  }
}
