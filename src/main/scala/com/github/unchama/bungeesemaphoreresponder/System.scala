package com.github.unchama.bungeesemaphoreresponder

import akka.actor.ActorSystem
import cats.effect.{ConcurrentEffect, ContextShift, IO}
import com.github.unchama.bungeesemaphoreresponder.bukkit.listeners.BungeeSemaphoreCooperator
import com.github.unchama.bungeesemaphoreresponder.domain.{BungeeSemaphoreSynchronization, PlayerFinalizerList, PlayerName}
import com.github.unchama.bungeesemaphoreresponder.infrastructure.redis.RedisBungeeSemaphoreSynchronization
import org.bukkit.entity.Player
import org.bukkit.event.Listener

class System[F[_] : ConcurrentEffect](playerFinalizerList: PlayerFinalizerList[F, Player])
                                     (implicit
                                      configuration: Configuration,
                                      _akkaSystem: ActorSystem,
                                      publishingContext: ContextShift[IO]) {
  val listenersToBeRegistered: Seq[Listener] = {
    implicit val _synchronization: BungeeSemaphoreSynchronization[F[Unit], PlayerName] = {
      new RedisBungeeSemaphoreSynchronization[F]()
    }
    Seq(
      new BungeeSemaphoreCooperator[F](playerFinalizerList)
    )
  }
}
