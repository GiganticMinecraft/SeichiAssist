package com.github.unchama.seichiassist.subsystems.buildcount.application.application

import cats.effect.{Clock, Sync}
import cats.implicits._
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.SinglePhasedRepositoryInitialization
import com.github.unchama.generic.ratelimiting.{FixedWindowRateLimiter, RateLimiter}
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.buildcount.application.Configuration
import com.github.unchama.seichiassist.subsystems.buildcount.domain.BuildAmountRateLimiterSnapshot
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountRateLimitPersistence
import io.chrisdavenport.cats.effect.time.JavaTime

import java.time.ZoneId
import java.util.concurrent.TimeUnit

object RateLimiterRepositoryDefinitions {

  import scala.concurrent.duration._

  def initialization[G[_]: Sync: JavaTime: Clock](
    implicit config: Configuration,
    persistence: BuildAmountRateLimitPersistence[G]
  ): SinglePhasedRepositoryInitialization[G, RateLimiter[G, BuildExpAmount]] = {
    val max = config.oneMinuteBuildExpLimit
    val span = 1.minute

    RefDictBackedRepositoryDefinition
      .usingUuidRefDictWithoutDefault(persistence)
      .initialization
      .extendPreparation { (_, _) => loadedRecordOpt =>
        {
          for {
            currentLocalTime <- JavaTime[G].getLocalDateTime(ZoneId.systemDefault())
            initialPermitCount = loadedRecordOpt.fold(max) { loadedRecord =>
              val duration = FiniteDuration(
                java.time.Duration.between(loadedRecord.recordTime, currentLocalTime).toNanos,
                TimeUnit.NANOSECONDS
              )
              // NOTE: これはファイナライゼーションされたときのレートリミッターと
              // イニシャライゼーションで作成されるレートリミッターが起動した時刻の差が
              // 規定時間の整数倍になっているとは限らないので多少の誤差を発生させることがある。
              // しかし、とりあえず趣旨を達成するためにこの実装を使う。
              // 必要であれば再度編集して同期を取るようにすること。
              if (duration >= span) {
                // expired
                max
              } else {
                loadedRecord.amount
              }
            }
            rateLimiter <- FixedWindowRateLimiter
              .in[G, BuildExpAmount](max, span, Some(initialPermitCount))
          } yield rateLimiter
        }
      }
  }

  def finalization[F[_]: Sync: JavaTime, Player: HasUuid](
    implicit config: Configuration,
    persistence: BuildAmountRateLimitPersistence[F]
  ): RepositoryFinalization[F, Player, RateLimiter[F, BuildExpAmount]] =
    RepositoryFinalization.withoutAnyFinalization {
      case (p, rateLimiter) =>
        for {
          currentRecord <- rateLimiter.peekAvailablePermissions
          persistenceRecord <- BuildAmountRateLimiterSnapshot.now(currentRecord)
          _ <- persistence.write(HasUuid[Player].of(p), persistenceRecord)
        } yield ()
    }
}
