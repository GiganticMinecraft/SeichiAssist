package com.github

import cats.effect.IO
import com.github.unchama.generic.ContextCoercion

package object unchama {

  extension [F[_], A](fa: F[A]) {
    def toIO(using coercion: ContextCoercion[F, IO]): IO[A] = coercion(fa)

    def runSync[G[_]](using coercion: ContextCoercion[F, G]): G[A] = coercion(fa)
  }

}
