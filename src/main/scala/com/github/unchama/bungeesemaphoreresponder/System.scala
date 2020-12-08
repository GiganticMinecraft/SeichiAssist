package com.github.unchama.bungeesemaphoreresponder

import cats.effect.ConcurrentEffect
import com.github.unchama.bungeesemaphoreresponder.bukkit.listeners.BungeeSemaphoreCooperator
import com.github.unchama.bungeesemaphoreresponder.domain.{BungeeSemaphoreSynchronization, PlayerFinalizerList}
import org.bukkit.entity.Player

class System[F[_] : ConcurrentEffect](val playerFinalizerList: PlayerFinalizerList[F, Player]) {

  implicit private val _synchronization: BungeeSemaphoreSynchronization[F[Unit], Player] = ???

  val listenersToBeRegistered = Seq(
    new BungeeSemaphoreCooperator[F](playerFinalizerList)
  )

}
