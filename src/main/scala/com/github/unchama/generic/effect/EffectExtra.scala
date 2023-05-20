package com.github.unchama.generic.effect

import cats.effect.{Effect, IO, Sync}

object EffectExtra {

  import cats.effect.implicits._

  def runAsyncAndForget[F[_]: Effect, G[_]: Sync, A](fa: F[A]): G[Unit] =
    fa.runAsync(_ => IO.unit).runSync[G]

}
