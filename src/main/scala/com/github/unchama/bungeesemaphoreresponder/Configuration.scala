package com.github.unchama.bungeesemaphoreresponder

import java.net.InetSocketAddress
import scala.concurrent.duration.Duration

trait RedisConnectionSettings {

  val host: String

  val port: Int

  val password: Option[String]

  lazy val address = new InetSocketAddress(host, port)

}

trait Configuration {

  val redis: RedisConnectionSettings

  val saveTimeoutDuration: Duration

}
