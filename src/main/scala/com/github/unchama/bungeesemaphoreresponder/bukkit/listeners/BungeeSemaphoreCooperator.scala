package com.github.unchama.bungeesemaphoreresponder.bukkit.listeners

import cats.ApplicativeError
import cats.effect.{Async, ConcurrentEffect, Timer}
import com.github.unchama.bungeesemaphoreresponder.Configuration
import com.github.unchama.bungeesemaphoreresponder.domain.actions.BungeeSemaphoreSynchronization
import com.github.unchama.bungeesemaphoreresponder.domain.{PlayerDataFinalizer, PlayerName}
import com.github.unchama.generic.EitherExtra
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

import scala.concurrent.duration.{Duration, FiniteDuration}

class BungeeSemaphoreCooperator[F[_]: ConcurrentEffect: Timer](
  finalizer: PlayerDataFinalizer[F, Player]
)(
  implicit synchronization: BungeeSemaphoreSynchronization[F[Unit], PlayerName],
  configuration: Configuration
) extends Listener {

  import cats.effect.implicits._
  import cats.implicits._

  @EventHandler(priority = EventPriority.LOWEST)
  def onQuit(event: PlayerQuitEvent): Unit = {
    val player = event.getPlayer
    val name = PlayerName(player.getName)
    val timeout = configuration.saveTimeoutDuration match {
      case duration: FiniteDuration => Timer[F].sleep(duration)
      case _: Duration.Infinite     => Async[F].never
    }

    val program = for {
      fiber <- finalizer.onQuitOf(player).attempt.start
      result <- ConcurrentEffect[F].race(timeout, fiber.join)
      _ <- EitherExtra.unassociate(result) match {
        case Left(error) =>
          synchronization.notifySaveFailureOf(name) >>
            error.traverse(ApplicativeError[F, Throwable].raiseError[Unit])
        case Right(_) =>
          synchronization.confirmSaveCompletionOf(name)
      }
    } yield ()

    program.toIO.unsafeRunAsyncAndForget()
  }
}
