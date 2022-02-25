package com.github.unchama.seichiassist.subsystems.chatratelimiter.domain

import cats.effect.concurrent.Ref
import cats.effect.{Sync, Timer}
import cats.implicits._
import com.github.unchama.datarepository.template.RepositoryDefinition.Phased.{SinglePhased, TwoPhased}
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.generic.ratelimiting.{FixedWindowRateLimiter, RateLimiter}
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI

import scala.concurrent.duration.DurationInt

object ChatRateLimitRepositoryDefinition {
  def withContext[
    F[_],
    G[_] : Sync : Timer,
    Player : HasUuid
  ](implicit breakCountAPI: BreakCountReadAPI[F, G, Player]): TwoPhased[G, Player, Ref[G, Option[RateLimiter[G, ChatCount]]]] = {
    TwoPhased(
      TwoPhasedRepositoryInitialization.augment(
        SinglePhased.trivial[G, Player].initialization
      )((p, _) => for {
        seichiAmount <- breakCountAPI.seichiAmountDataRepository(p).read
        rateLimiter <- FixedWindowRateLimiter.in[G, ChatCount](ChatCount.One, 30.seconds)
        ref <- Ref[G].of(Option.when(seichiAmount.levelCorrespondingToExp.level == 1)(rateLimiter))
      } yield ref),
      // does not need any finalization
      RepositoryFinalization.trivial
    )
  }
}
