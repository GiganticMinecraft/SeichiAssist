package com.github.unchama.bungeesemaphoreresponder.bukkit.listeners

import cats.effect.ConcurrentEffect
import com.github.unchama.bungeesemaphoreresponder.Configuration
import com.github.unchama.bungeesemaphoreresponder.domain.{BungeeSemaphoreSynchronization, PlayerFinalizerList, PlayerName}
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class BungeeSemaphoreCooperator[
  F[_] : ConcurrentEffect
](registry: PlayerFinalizerList[F, Player])
 (implicit synchronization: BungeeSemaphoreSynchronization[F[Unit], PlayerName],
  configuration: Configuration) extends Listener {

  import cats.effect.implicits._
  import cats.implicits._

  @EventHandler(priority = EventPriority.LOWEST)
  def onQuit(event: PlayerQuitEvent): Unit = {
    val player = event.getPlayer
    val name = PlayerName(player.getName)

    val program = for {
      fibers <- registry
        .allActionsOnQuitOf(player)
        .traverse(_.attempt.start)
      results <- fibers.traverse(_.join)
      _ <-
        if (results.forall(_.isRight))
          synchronization.confirmSaveCompletionOf(name)
        else
          synchronization.notifySaveFailureOf(name)
    } yield ()

    program.toIO.unsafeRunAsyncAndForget()
  }
}
