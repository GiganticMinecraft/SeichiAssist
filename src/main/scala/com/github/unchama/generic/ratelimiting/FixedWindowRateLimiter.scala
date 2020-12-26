package com.github.unchama.generic.ratelimiting

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Sync, Timer}
import com.github.unchama.generic.ContextCoercion

import scala.concurrent.duration.FiniteDuration
import scala.ref.WeakReference

object FixedWindowRateLimiter {

  import ContextCoercion._
  import cats.implicits._

  def in[
    F[_] : Concurrent : Timer,
    G[_] : Sync : ContextCoercion[*[_], F]
  ](maxPermits: Int, resetDuration: FiniteDuration): F[RateLimiter[G]] =
    for {
      permitRef <- Ref.of[G, Int](maxPermits).coerceTo[F]

      rateLimiter = RateLimiter.fromPermitRef(permitRef)
      refreshPermits = permitRef.set(maxPermits).coerceTo[F]

      rateLimiterRef = new WeakReference(rateLimiter)
      rateLimiterStillActive = Sync[F].delay(rateLimiterRef.get.nonEmpty)
      refreshRoutine = Monad[F].untilDefinedM {
        for {
          _ <- Timer[F].sleep(resetDuration)
          _ <- refreshPermits
          active <- rateLimiterStillActive
        } yield Option.when(!active)(())
      }

      _ <- Concurrent[F].start(refreshRoutine)
    } yield rateLimiter
}
