package com.github.unchama.seichiassist.subsystems.buildcount.application.application

import cats.effect.{ConcurrentEffect, Sync, Timer}
import com.github.unchama.datarepository.template.{RepositoryFinalization, SinglePhasedRepositoryInitialization}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.ratelimiting.{FixedWindowRateLimiter, RateLimiter}
import com.github.unchama.seichiassist.subsystems.buildcount.application.Configuration
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount

object RateLimiterRepositoryDefinitions {

  import scala.concurrent.duration._

  def initialization[
    F[_] : ConcurrentEffect : Timer,
    G[_] : Sync : ContextCoercion[*[_], F]
  ](implicit config: Configuration): SinglePhasedRepositoryInitialization[G, RateLimiter[G, BuildExpAmount]] =
    SinglePhasedRepositoryInitialization.withSupplier {
      FixedWindowRateLimiter.in[F, G, BuildExpAmount](
        config.oneMinuteBuildExpLimit,
        1.minute
      )
    }

  def finalization[F[_] : Sync, Player]: RepositoryFinalization[F, Player, RateLimiter[F, BuildExpAmount]] =
    RepositoryFinalization.trivial

}
