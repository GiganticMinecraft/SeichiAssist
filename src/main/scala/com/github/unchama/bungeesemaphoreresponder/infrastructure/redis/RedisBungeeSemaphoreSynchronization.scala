package com.github.unchama.bungeesemaphoreresponder.infrastructure.redis

import org.apache.pekko.actor.ActorSystem
import cats.effect.Async
import cats.syntax.all._
import com.github.unchama.bungeesemaphoreresponder.Configuration
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerName
import com.github.unchama.bungeesemaphoreresponder.domain.actions.BungeeSemaphoreSynchronization
import com.github.unchama.bungeesemaphoreresponder.infrastructure.redis.SignalFormat.{
  BungeeSemaphoreMessage,
  DataSaveFailed,
  ReleaseDataLock
}

class RedisBungeeSemaphoreSynchronization[F[_]: Async](
  implicit configuration: Configuration,
  actorSystem: ActorSystem
) extends BungeeSemaphoreSynchronization[F[Unit], PlayerName] {

  private val client = ConfiguredRedisClient()

  type Action = F[Unit]

  private def sendMessage(message: BungeeSemaphoreMessage): Action = {
    Async[F].fromFuture {
      Async[F].delay {
        message match {
          case ReleaseDataLock(playerName) => client.del(SignalFormat.lockKeyOf(playerName))
          case DataSaveFailed(playerName)  =>
            client.pexpire(SignalFormat.lockKeyOf(playerName), 1)
        }
      }
    }.void
  }

  override def confirmSaveCompletionOf(player: PlayerName): Action = sendMessage(
    ReleaseDataLock(player)
  )

  override def notifySaveFailureOf(player: PlayerName): Action = sendMessage(
    DataSaveFailed(player)
  )

}
