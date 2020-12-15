package com.github.unchama.seichiassist.infrastructure.logging.jul

import java.util.logging.{Level, LogRecord, Logger}

class NamedJULLogger(newName: String, existingLogger: Logger) extends Logger(newName, null) {
  {
    setLevel(Level.ALL)
  }

  override def log(record: LogRecord): Unit = existingLogger.log(record)

}
