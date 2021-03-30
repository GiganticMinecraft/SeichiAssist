package com.github.unchama.generic

import cats.effect.concurrent.Ref

object RefExtra {

  def getAndUpdateAndGet[F[_], A](ref: Ref[F, A])(f: A => A): F[(A, A)] =
    ref.modify { oldA =>
      val newA = f(oldA)
      (newA, (oldA, newA))
    }

}
