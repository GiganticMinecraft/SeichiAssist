package com.github.unchama.seichiassist.infrastructure

import scalikejdbc.{ConnectionPool, ConnectionPoolSettings, GlobalSettings, LoggingSQLAndTimeSettings}

object ScalikeJDBCConfiguration {

  val connectionPoolSettings: ConnectionPoolSettings = ConnectionPoolSettings(
    initialSize = 5,
    maxSize = 20,
    connectionTimeoutMillis = 100000L
  )

  val loggingSettings: LoggingSQLAndTimeSettings = LoggingSQLAndTimeSettings(
    enabled = true,
    singleLineMode = true,
    printUnprocessedStackTrace = false,
    stackTraceDepth = 15,
    logLevel = Symbol("debug"),
    warningEnabled = false
  )

  def initializeConnectionPool(url: String, user: String, password: String): Unit = {
    ConnectionPool.singleton(url, user, password, connectionPoolSettings)
  }

  def initializeGlobalConfigs(): Unit = {
    GlobalSettings.loggingSQLAndTime = loggingSettings
  }

}
