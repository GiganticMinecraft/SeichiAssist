package com.github.unchama.util.effect

import cats.effect.IO

object IOUtils {

  def forever(program: IO[Any]): IO[Nothing] = program.flatMap(_ => forever(program))

}
