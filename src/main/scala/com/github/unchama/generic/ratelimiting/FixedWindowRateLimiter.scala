package com.github.unchama.generic.ratelimiting

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ConcurrentEffect, IO, Sync, Timer}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.algebra.typeclasses.OrderedMonus

import scala.concurrent.duration.FiniteDuration
import scala.ref.WeakReference

object FixedWindowRateLimiter {

  import ContextCoercion._
  import cats.effect.implicits._
  import cats.implicits._

  def in[
    F[_] : ConcurrentEffect : Timer,
    G[_] : Sync : ContextCoercion[*[_], F],
    A: OrderedMonus
  ](maxPermits: A, resetDuration: FiniteDuration): G[RateLimiter[G, A]] = {
    val zero = OrderedMonus[A].empty

    for {
      countRef <- Ref.of[G, A](zero)

      rateLimiter = RateLimiter.fromCountRef(countRef)(maxPermits)
      refreshPermits = countRef.set(zero).coerceTo[F]

      rateLimiterRef = new WeakReference(rateLimiter)
      rateLimiterStillActive = Sync[F].delay(rateLimiterRef.get.nonEmpty)
      refreshRoutine = Monad[F].untilDefinedM {
        for {
          _ <- Timer[F].sleep(resetDuration)
          _ <- refreshPermits
          active <- rateLimiterStillActive
        } yield Option.when(!active)(())
      }

      _ <- Concurrent[F]
        .start(refreshRoutine)
        .runAsync(_ => IO.unit)
        .runSync[G]
    } yield rateLimiter
  }
}
