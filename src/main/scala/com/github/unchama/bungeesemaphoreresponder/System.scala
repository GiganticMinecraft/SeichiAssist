package com.github.unchama.bungeesemaphoreresponder

import org.apache.pekko.actor.ActorSystem
import cats.effect.Async
import com.github.unchama.bungeesemaphoreresponder.bukkit.listeners.BungeeSemaphoreCooperator
import com.github.unchama.bungeesemaphoreresponder.domain.actions.BungeeSemaphoreSynchronization
import com.github.unchama.bungeesemaphoreresponder.domain.{PlayerDataFinalizer, PlayerName}
import com.github.unchama.bungeesemaphoreresponder.infrastructure.redis.RedisBungeeSemaphoreSynchronization
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.entity.Player
import org.bukkit.event.Listener

class System[F[_]: Async](val finalizers: List[PlayerDataFinalizer[F, Player]])(
  implicit configuration: Configuration,
  _akkaSystem: ActorSystem,
  effectEnvironment: EffectEnvironment[F]
) {
  val listenersToBeRegistered: Seq[Listener] = {
    implicit val _synchronization: BungeeSemaphoreSynchronization[F[Unit], PlayerName] = {
      new RedisBungeeSemaphoreSynchronization[F]()
    }
    Seq(new BungeeSemaphoreCooperator[F](finalizers))
  }
}
