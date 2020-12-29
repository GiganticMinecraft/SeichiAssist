package com.github.unchama.seichiassist.subsystems.buildcount.bukkit.datarepository

import cats.Applicative
import cats.effect.{ConcurrentEffect, SyncEffect, Timer}
import com.github.unchama.datarepository.bukkit.player.TwoPhasedPlayerDataRepository
import com.github.unchama.generic.ratelimiting.{FixedWindowRateLimiter, RateLimiter}
import com.github.unchama.seichiassist.subsystems.buildcount.application.Configuration
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import org.bukkit.entity.Player

import java.util.UUID

class RateLimiterRepository[
  F[_] : ConcurrentEffect : Timer,
  G[_] : SyncEffect
](implicit config: Configuration) extends TwoPhasedPlayerDataRepository[G, RateLimiter[G, BuildExpAmount]] {

  import cats.implicits._

  import scala.concurrent.duration._

  override protected type TemporaryData = RateLimiter[G, BuildExpAmount]
  type Data = RateLimiter[G, BuildExpAmount]

  override protected val loadTemporaryData: (String, UUID) => G[Either[Option[String], Data]] =
    (_, _) =>
      FixedWindowRateLimiter.in[F, G, BuildExpAmount](
        config.oneMinuteBuildExpLimit,
        1.second
      ).map(Right.apply)

  override protected def initializeValue(player: Player, temporaryData: TemporaryData): G[Data] =
    Applicative[G].pure(temporaryData)

  override protected val finalizeBeforeUnload: (Player, Data) => G[Unit] =
    (_, _) => Applicative[G].unit
}
