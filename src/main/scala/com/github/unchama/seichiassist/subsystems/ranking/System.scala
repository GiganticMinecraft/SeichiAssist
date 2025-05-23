package com.github.unchama.seichiassist.subsystems.ranking

import cats.effect.{Concurrent, Timer}
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.ranking.api.{
  AssortedRankingApi,
  RankingProvider
}
import com.github.unchama.seichiassist.subsystems.ranking.application.GenericRefreshingRankingCache
import com.github.unchama.seichiassist.subsystems.ranking.domain.values.{LoginTime, VoteCount}
import com.github.unchama.seichiassist.subsystems.ranking.infrastructure._
import io.chrisdavenport.log4cats.ErrorLogger

import scala.concurrent.duration.DurationInt

object System {

  import cats.implicits._

  def wired[F[_]: Timer: Concurrent: ErrorLogger, H[_]]: F[AssortedRankingApi[F]] =
    for {
      seichiRanking <- GenericRefreshingRankingCache.withPersistence(
        new JdbcSeichiRankingRecordPersistence[F],
        30.seconds
      )
      buildRanking <- GenericRefreshingRankingCache.withPersistence(
        new JdbcBuildRankingRecordPersistence[F],
        30.seconds
      )
      loginRanking <- GenericRefreshingRankingCache.withPersistence(
        new JdbcLoginRankingRecordPersistence[F],
        1.hours
      )
      voteRanking <- GenericRefreshingRankingCache.withPersistence(
        new JdbcVoteRankingRecordPersistence[F],
        1.hours
      )
    } yield {
      new AssortedRankingApi[F] {
        override val seichiAmountRanking: RankingProvider[F, SeichiAmountData] =
          RankingProvider(seichiRanking)
        override val buildAmountRanking: RankingProvider[F, BuildAmountData] = RankingProvider(
          buildRanking
        )
        override val loginTimeRanking: RankingProvider[F, LoginTime] = RankingProvider(
          loginRanking
        )
        override val voteCountRanking: RankingProvider[F, VoteCount] = RankingProvider(
          voteRanking
        )
      }
    }
}
