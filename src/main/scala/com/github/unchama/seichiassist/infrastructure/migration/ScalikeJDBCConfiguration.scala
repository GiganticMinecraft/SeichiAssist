package com.github.unchama.seichiassist.infrastructure.migration

import scalikejdbc._

object ScalikeJDBCConfiguration {

  val settings: ConnectionPoolSettings = ConnectionPoolSettings(
    initialSize = 5,
    maxSize = 20,
    connectionTimeoutMillis = 100000L
  )

  def initializeConnectionPool(url: String, user: String, password: String): Unit = {
    ConnectionPool.singleton(url, user, password, settings)
  }

}
