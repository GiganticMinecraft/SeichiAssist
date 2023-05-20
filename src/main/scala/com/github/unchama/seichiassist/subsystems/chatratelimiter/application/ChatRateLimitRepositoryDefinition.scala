package com.github.unchama.seichiassist.subsystems.chatratelimiter.application

import cats.effect.{Clock, Sync}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition.Phased.SinglePhased
import com.github.unchama.generic.ratelimiting.{FixedWindowRateLimiter, RateLimiter}
import com.github.unchama.seichiassist.subsystems.chatratelimiter.domain.ChatCount

import scala.concurrent.duration._

object ChatRateLimitRepositoryDefinition {
  def inSyncContext[G[_]: Sync: Clock, Player]
    : RepositoryDefinition[G, Player, RateLimiter[G, ChatCount]] = {
    SinglePhased.withSupplierAndTrivialFinalization(
      FixedWindowRateLimiter.in[G, ChatCount](ChatCount.One, 30.seconds)
    )
  }
}
