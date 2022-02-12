package com.github.unchama.datarepository.template.initialization

import cats.effect.{ConcurrentEffect, Sync, Timer}
import cats.implicits._
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.ContextCoercion.coercibleComputation
import com.github.unchama.generic.algebra.typeclasses.OrderedMonus
import com.github.unchama.generic.ratelimiting.{FixedWindowRateLimiter, RateLimiter}
import scalikejdbc._

import scala.concurrent.duration.FiniteDuration

object RepositoryInitializationExt {
  implicit class ForSinglePhased[
    F[_]: ContextCoercion[*[_], G]: Sync,
    G[_]: ConcurrentEffect: Timer,
    R: OrderedMonus
  ](self: SinglePhasedRepositoryInitialization[F, RateLimiter[F, R]]) {
    def overwriteWithDatabaseValue(key: String, upperLimit: R, reset: FiniteDuration)
                                  (extractor: WrappedResultSet => R): SinglePhasedRepositoryInitialization[F, RateLimiter[F, R]] = {
      self.extendPreparation { case (uuid, _) =>
        _ => for {
          newRateLimiter <- FixedWindowRateLimiter.in[G, F, R](upperLimit, reset)
          usedPermission <- Sync[F].delay {
            DB.readOnly { implicit session =>
              sql"""SELECT * FROM player_rate_limit where uuid = $uuid and rate_limit_name = $key"""
                .map(extractor)
                .single()
                .apply()
            }.getOrElse(OrderedMonus[R].empty)
          }
          _ <- newRateLimiter.requestPermission(usedPermission)
        } yield newRateLimiter
      }
    }
  }
}

