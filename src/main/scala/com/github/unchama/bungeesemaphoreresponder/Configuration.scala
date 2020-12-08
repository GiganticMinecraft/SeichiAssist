package com.github.unchama.bungeesemaphoreresponder

import java.net.InetSocketAddress

trait RedisConnectionSettings {

  val host: String

  val port: Int

  val password: Option[String]

  lazy val address = new InetSocketAddress(host, port)

}

trait Configuration {

  val redis: RedisConnectionSettings

}
