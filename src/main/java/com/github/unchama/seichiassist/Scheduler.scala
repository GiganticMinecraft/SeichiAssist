package com.github.unchama.seichiassist

import cats.effect.{ContextShift, IO}

object Schedulers {

  val sync: ContextShift[IO] = ???

  val async: ContextShift[IO] = ???

}
