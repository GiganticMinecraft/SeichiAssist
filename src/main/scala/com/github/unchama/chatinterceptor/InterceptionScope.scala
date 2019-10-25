package com.github.unchama.chatinterceptor

import cats.effect.concurrent.Deferred
import cats.effect.{ContextShift, IO}
import com.github.unchama.chatinterceptor.CancellationReason.Overridden
import com.github.unchama.chatinterceptor.InterceptorResponse.{Ignored, Intercepted}

import scala.collection.mutable

sealed trait CancellationReason
object CancellationReason {
  case object PlayerQuit extends CancellationReason
  case object Overridden extends CancellationReason
}

sealed trait InterceptorResponse
object InterceptorResponse {
  case object Intercepted extends InterceptorResponse
  case object Ignored extends InterceptorResponse
}

class InterceptionScope[K, R](implicit val cs: ContextShift[IO]) {
  import InterceptionScope._

  private val map: mutable.Map[K, Deferred[IO, InterceptionResult[R]]] = mutable.HashMap()

  def cancelAnyInterception(key: K, reason: CancellationReason): IO[Unit] =
    IO { map.remove(key) }
      .flatMap {
        case Some(deferred) => deferred.complete(Right(reason))
        case None => IO.pure(())
      }

  def interceptFrom(key: K): IO[InterceptionResult[R]] = for {
    deferred <- Deferred[IO, InterceptionResult[R]]
    _ <- cancelAnyInterception(key, Overridden)
    _ <- IO { map.put(key, deferred) }
    result <- deferred.get
  } yield result

  def suggestInterception(key: K, response: R): IO[InterceptorResponse] =
    IO { map.remove(key) }.flatMap {
      case Some(deferred) => for {
        _ <- deferred.complete(Left(response))
      } yield Intercepted
      case None => IO.pure(Ignored)
    }
}

object InterceptionScope {
  type InterceptionResult[R] = Either[R, CancellationReason]
}
