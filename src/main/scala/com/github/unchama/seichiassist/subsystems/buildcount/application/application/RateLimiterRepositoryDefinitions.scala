package com.github.unchama.seichiassist.subsystems.buildcount.application.application

import cats.Monad
import cats.effect.{ConcurrentEffect, Sync, Timer}
import cats.implicits._
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.SinglePhasedRepositoryInitialization
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.ContextCoercion._
import com.github.unchama.generic.algebra.typeclasses.OrderedMonus
import com.github.unchama.generic.ratelimiting.{FixedWindowRateLimiter, RateLimiter}
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.buildcount.application.Configuration
import com.github.unchama.seichiassist.subsystems.buildcount.domain.BuildAmountRateLimiterSnapshot
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountRateLimitPersistence
import io.chrisdavenport.cats.effect.time.JavaTime

import java.util.concurrent.TimeUnit

object RateLimiterRepositoryDefinitions {

  import scala.concurrent.duration._

  def initialization[
    F[_] : ConcurrentEffect : Timer,
    G[_] : Sync: ContextCoercion[*[_], F] : JavaTime
  ](
     implicit config: Configuration,
     persistence: BuildAmountRateLimitPersistence[G]
   ): SinglePhasedRepositoryInitialization[G, RateLimiter[G, BuildExpAmount]] = {
    val max = config.oneMinuteBuildExpLimit
    val span = 1.minute
    val rateLimiter = BuildAmountRateLimiterSnapshot.now(max).flatMap { bapr =>
      FixedWindowRateLimiter.in[F, G, BuildExpAmount](
        bapr.amount,
        span
      )
    }

    val maxValueWithCurrentTimeG = BuildAmountRateLimiterSnapshot.now[G](max)
    RefDictBackedRepositoryDefinition.usingUuidRefDictWithEffectfulDefault(persistence)(maxValueWithCurrentTimeG)
      .initialization
      .extendPreparation { (_, _) =>
        loadedRecord =>
          // NOTE: これはファイナライゼーションされたときのレートリミッターと
          // イニシャライゼーションで作成されるレートリミッターの 時刻起点が
          // スパンの倍数になっているとは限らないので多少の誤差を発生させるが、
          // 趣旨を達成するためにとりあえずこの実装を使う。
          // 必要であれば再度編集して同期を取るようにすること。
          for {
            maxValueWithCurrentTime <- maxValueWithCurrentTimeG
            duration = FiniteDuration(
              java.time.Duration
                .between(loadedRecord.recordTime, maxValueWithCurrentTime.recordTime)
                .toNanos,
              TimeUnit.NANOSECONDS
            )
            // 記録した日時が十分に新しければ更新
            postInitialization = (rateLimiter: RateLimiter[G, BuildExpAmount]) => {
              if (duration >= span) {
                // it's expired, do nothing.
                Monad[G].pure(())
              } else {
                // it's still active
                val consumedPermission = OrderedMonus[BuildExpAmount].|-|(max, loadedRecord.amount)
                rateLimiter.requestPermission(consumedPermission).void
              }
            }
            rateLimiter <- rateLimiter.flatTap(postInitialization)
          } yield rateLimiter
      }
  }

  def finalization[
    F[_] : Sync : JavaTime,
    Player: HasUuid
  ](implicit config: Configuration, persistence: BuildAmountRateLimitPersistence[F]): RepositoryFinalization[F, Player, RateLimiter[F, BuildExpAmount]] =
    RepositoryFinalization.withoutAnyFinalization { case (p, rateLimiter) =>
      for {
        currentRecord <- rateLimiter.peekAvailablePermission
        persistenceRecord <- BuildAmountRateLimiterSnapshot.now(currentRecord)
        _ <- persistence.write(HasUuid[Player].of(p), persistenceRecord)
      } yield ()
    }
}
