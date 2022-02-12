package com.github.unchama.seichiassist.subsystems.buildcount.application.application

import cats.effect.{ConcurrentEffect, Sync, Timer}
import cats.implicits._
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.RepositoryInitializationExt.ForSinglePhased
import com.github.unchama.datarepository.template.initialization.SinglePhasedRepositoryInitialization
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.ratelimiting.{FixedWindowRateLimiter, RateLimiter}
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.buildcount.application.Configuration
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import scalikejdbc._

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
    }.overwriteWithDatabaseValue("buildcount", config.oneMinuteBuildExpLimit, 1.minute) {
      wrs => BuildExpAmount(wrs.bigDecimal("buildcount"))
    }

  def finalization[F[_] : Sync, Player: HasUuid](implicit config: Configuration): RepositoryFinalization[F, Player, RateLimiter[F, BuildExpAmount]] =
    RepositoryFinalization.withoutAnyFinalization { case (p, rateLimiter) =>
      // NOTE: １分間のタイムスライスで、レートリミッターの残量の最大値はコンフィグで規定された値なので、
      // これをすることによってどのぐらい残量があるか確認できる。
      rateLimiter.requestPermission(BuildExpAmount.ofNonNegative(config.oneMinuteBuildExpLimit.amount)).map { bea =>
        DB.localTx { implicit session =>
          sql"""INSERT INTO player_rate_limit VALUES(${HasUuid[Player].of(p)}, "buildcount", ${bea.toPlainString})"""
            .execute()
            .apply()
        }
      }
    }

}
