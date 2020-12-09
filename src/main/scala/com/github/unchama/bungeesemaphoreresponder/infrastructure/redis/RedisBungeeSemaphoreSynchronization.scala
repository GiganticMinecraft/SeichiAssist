package com.github.unchama.bungeesemaphoreresponder.infrastructure.redis

import akka.actor.ActorSystem
import cats.effect.{ContextShift, Effect, IO}
import com.github.unchama.bungeesemaphoreresponder.Configuration
import com.github.unchama.bungeesemaphoreresponder.domain.{BungeeSemaphoreSynchronization, PlayerName}
import com.github.unchama.bungeesemaphoreresponder.infrastructure.redis.SignalFormat.{BungeeSemaphoreMessage, DataSaveFailed, ReleaseDataLock}

class RedisBungeeSemaphoreSynchronization[F[_] : Effect](implicit
                                                         publishingContext: ContextShift[IO],
                                                         configuration: Configuration,
                                                         actorSystem: ActorSystem)
  extends BungeeSemaphoreSynchronization[F[Unit], PlayerName] {

  private val client = ConfiguredRedisClient()

  type Action = F[Unit]

  private def sendMessage(message: BungeeSemaphoreMessage): Action = {
    Effect[F].liftIO {
      IO.fromFuture {
        IO {
          client.publish(SignalFormat.signalingChannel, message.toString)
        }
      }.as(())
    }
  }

  override def confirmSaveCompletionOf(player: PlayerName): Action = sendMessage(ReleaseDataLock(player))

  override def notifySaveFailureOf(player: PlayerName): Action = sendMessage(DataSaveFailed(player))

}
