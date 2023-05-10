package com.github.unchama.bungeesemaphoreresponder.bukkit.listeners

import cats.ApplicativeError
import cats.data.Validated
import cats.effect.{Async, ConcurrentEffect, Sync, Timer}
import com.github.unchama.bungeesemaphoreresponder.Configuration
import com.github.unchama.bungeesemaphoreresponder.domain.actions.BungeeSemaphoreSynchronization
import com.github.unchama.bungeesemaphoreresponder.domain.{PlayerDataFinalizer, PlayerName}
import com.github.unchama.generic.effect.ConcurrentExtra.attemptInParallel
import com.github.unchama.generic.effect.MonadThrowExtra.retryUntilSucceeds
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

import scala.concurrent.duration.{Duration, FiniteDuration}

class BungeeSemaphoreCooperator[F[_]: ConcurrentEffect: Timer](
  finalizers: List[PlayerDataFinalizer[F, Player]]
)(
  implicit synchronization: BungeeSemaphoreSynchronization[F[Unit], PlayerName],
  configuration: Configuration,
  effectEnvironment: EffectEnvironment
) extends Listener {

  @EventHandler(priority = EventPriority.LOWEST)
  def onQuit(event: PlayerQuitEvent): Unit = {
    val player = event.getPlayer
    val name = PlayerName(player.getName)
    val timeout = configuration.saveTimeoutDuration match {
      case duration: FiniteDuration => Timer[F].sleep(duration)
      case _: Duration.Infinite     => Async[F].never
    }

    case object TimeoutReached
        extends Exception(s"Timeout ${configuration.saveTimeoutDuration} reached!")

    val quitProcess = attemptInParallel(
      finalizers.map(finalizer => retryUntilSucceeds(finalizer.onQuitOf(player))(10))
    )

    import cats.implicits._

    val program = for {
      raceResult <- ConcurrentEffect[F].race(timeout, quitProcess)
      _ <- raceResult match {
        case Left(_) =>
          synchronization.notifySaveFailureOf(name) >> ApplicativeError[F, Throwable]
            .raiseError[Unit](TimeoutReached)
        case Right(results) =>
          results.traverse(e => Validated.fromEither(e).leftMap(List.apply(_))) match {
            case Validated.Valid(_) =>
              synchronization.confirmSaveCompletionOf(name)
            case Validated.Invalid(errors) =>
              synchronization.notifySaveFailureOf(name) >> errors.traverse(error =>
                Sync[F].delay {
                  error.printStackTrace()
                }
              )
          }
      }
    } yield ()

    effectEnvironment.unsafeRunEffectAsync("[BungeeSemaphoreCooperator] 終了処理を実行する", program)
  }
}
