package com.github.unchama.buildassist.bukkit.datarepository

import cats.Applicative
import cats.effect.{ConcurrentEffect, SyncEffect, Timer}
import com.github.unchama.buildassist.application.Configuration
import com.github.unchama.datarepository.bukkit.player.TwoPhasedPlayerDataRepository
import com.github.unchama.generic.ratelimiting.{FixedWindowRateLimiter, RateLimiter}
import org.bukkit.entity.Player

import java.util.UUID

class RateLimiterRepository[
  F[_] : ConcurrentEffect : Timer,
  G[_] : SyncEffect
](implicit config: Configuration) extends TwoPhasedPlayerDataRepository[G, RateLimiter[G]] {

  import cats.implicits._

  import scala.concurrent.duration._

  override protected type TemporaryData = RateLimiter[G]
  override protected val loadTemporaryData: (String, UUID) => G[Either[Option[String], RateLimiter[G]]] =
    (_, _) =>
      FixedWindowRateLimiter.in[F, G](
        config.oneMinuteBuildExpLimit.floor,
        1.second
      ).map(Right.apply)

  override protected def initializeValue(player: Player, temporaryData: RateLimiter[G]): G[RateLimiter[G]] =
    Applicative[G].pure(temporaryData)

  override protected val finalizeBeforeUnload: (Player, RateLimiter[G]) => G[Unit] =
    (_, _) => Applicative[G].unit
}
