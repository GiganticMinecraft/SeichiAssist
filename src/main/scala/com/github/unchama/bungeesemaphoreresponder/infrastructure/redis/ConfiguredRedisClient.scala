package com.github.unchama.bungeesemaphoreresponder.infrastructure.redis

import akka.actor.ActorSystem
import com.github.unchama.bungeesemaphoreresponder.Configuration
import redis.RedisClient

object ConfiguredRedisClient {

  def apply()(implicit configuration: Configuration, actorSystem: ActorSystem): RedisClient = {
    RedisClient(
      host = configuration.redis.host,
      port = configuration.redis.port,
      password = configuration.redis.password
    )
  }

}
