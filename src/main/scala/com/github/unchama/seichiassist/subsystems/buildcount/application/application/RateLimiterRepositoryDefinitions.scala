package com.github.unchama.seichiassist.subsystems.buildcount.application.application

import cats.effect.{ConcurrentEffect, Sync, Timer}
import cats.implicits._
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.SinglePhasedRepositoryInitialization
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.ratelimiting.{FixedWindowRateLimiter, RateLimiter}
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.buildcount.application.Configuration
import com.github.unchama.seichiassist.subsystems.buildcount.domain.BuildAmountPermission
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountRateLimitPersistence
import scalikejdbc._

object RateLimiterRepositoryDefinitions {

  import scala.concurrent.duration._

  def initialization[
    F[_] : ConcurrentEffect : Timer,
    G[_] : Sync : ContextCoercion[*[_], F]
  ](
     implicit config: Configuration,
     persistence: BuildAmountRateLimitPersistence[G]
   ): SinglePhasedRepositoryInitialization[G, RateLimiter[G, BuildAmountPermission]] = {
    val rateLimiter = FixedWindowRateLimiter.in[F, G, BuildAmountPermission](
      BuildAmountPermission(config.oneMinuteBuildExpLimit),
      1.minute
    )

    RefDictBackedRepositoryDefinition.usingUuidRefDict(persistence)(BuildAmountPermission.orderedMonus.empty)
      .initialization
      .extendPreparation { (_, _) => usedPermission =>
        rateLimiter.flatTap(rateLimiter => rateLimiter.requestPermission(usedPermission))
      }
  }

  def finalization[F[_] : Sync, Player: HasUuid](implicit config: Configuration, persistence: BuildAmountRateLimitPersistence[F]): RepositoryFinalization[F, Player, RateLimiter[F, BuildAmountPermission]] =
    RepositoryFinalization.withoutAnyFinalization { case (p, rateLimiter) =>
      rateLimiter.peekAvailablePermission.flatMap(a => persistence.write(HasUuid[Player].of(p), a))
    }
}
