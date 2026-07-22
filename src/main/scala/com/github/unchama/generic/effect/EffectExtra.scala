package com.github.unchama.generic.effect

import cats.effect.Sync
import cats.effect.std.Dispatcher

object EffectExtra {

  def runAsyncAndForget[F[_], G[_]: Sync, A](fa: F[A])(
    implicit dispatcher: Dispatcher[F]
  ): G[Unit] =
    Sync[G].delay(dispatcher.unsafeRunAndForget(fa))

}
