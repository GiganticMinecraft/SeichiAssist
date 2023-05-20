package com.github.unchama.util.logging.log4cats

import io.chrisdavenport.log4cats.Logger

object PrefixedLogger {

  def apply[F[_]: Logger](prefix: String): Logger[F] = TransformingLogger(prefix + _)

}
