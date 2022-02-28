package com.github.unchama.bungeesemaphoreresponder.infrastructure.redis

import akka.actor.ActorSystem
import cats.effect.{ContextShift, Effect, IO}
import com.github.unchama.bungeesemaphoreresponder.Configuration
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerName
import com.github.unchama.bungeesemaphoreresponder.domain.actions.BungeeSemaphoreSynchronization
import com.github.unchama.bungeesemaphoreresponder.infrastructure.redis.SignalFormat.{
  BungeeSemaphoreMessage,
  DataSaveFailed,
  ReleaseDataLock
}

class RedisBungeeSemaphoreSynchronization[F[_]: Effect](
  implicit publishingContext: ContextShift[IO],
  configuration: Configuration,
  actorSystem: ActorSystem
) extends BungeeSemaphoreSynchronization[F[Unit], PlayerName] {

  private val client = ConfiguredRedisClient()

  type Action = F[Unit]

  private def sendMessage(message: BungeeSemaphoreMessage): Action = {
    Effect[F].liftIO {
      IO.fromFuture {
        IO {
          message match {
            case ReleaseDataLock(playerName) =>
              client.del(SignalFormat.lockKeyOf(playerName))
            case DataSaveFailed(playerName) =>
              client.pexpire(SignalFormat.lockKeyOf(playerName), 1)
          }
        }
      }.as(())
    }
  }

  override def confirmSaveCompletionOf(player: PlayerName): Action = sendMessage(
    ReleaseDataLock(player)
  )

  override def notifySaveFailureOf(player: PlayerName): Action = sendMessage(
    DataSaveFailed(player)
  )

}
