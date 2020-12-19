package com.github.unchama.bungeesemaphoreresponder.bukkit.listeners

import cats.effect.{Async, ConcurrentEffect, Timer}
import com.github.unchama.bungeesemaphoreresponder.Configuration
import com.github.unchama.bungeesemaphoreresponder.domain.{BungeeSemaphoreSynchronization, PlayerDataFinalizerList, PlayerName}
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

import scala.concurrent.duration.{Duration, FiniteDuration}

class BungeeSemaphoreCooperator[
  F[_] : ConcurrentEffect : Timer
](registry: PlayerDataFinalizerList[F, Player])
 (implicit synchronization: BungeeSemaphoreSynchronization[F[Unit], PlayerName],
  configuration: Configuration) extends Listener {

  import cats.effect.implicits._
  import cats.implicits._

  @EventHandler(priority = EventPriority.LOWEST)
  def onQuit(event: PlayerQuitEvent): Unit = {
    val player = event.getPlayer
    val name = PlayerName(player.getName)
    val timeout = configuration.saveTimeoutDuration

    println("QuitEvent called!")

    val program = for {
      fibers <- registry
        .allActionsOnQuitOf(player).toList
        .traverse(_.attempt.start)
      results <-
        ConcurrentEffect[F].race(
          timeout match {
            case duration: FiniteDuration => Timer[F].sleep(duration)
            case _: Duration.Infinite => Async[F].never
          },
          fibers.traverse(_.join)
        )
      _ <-
        if (results.exists(_.forall(_.isRight)))
          synchronization.confirmSaveCompletionOf(name)
        else
          synchronization.notifySaveFailureOf(name)
    } yield ()

    program.toIO.unsafeRunAsyncAndForget()
  }
}
