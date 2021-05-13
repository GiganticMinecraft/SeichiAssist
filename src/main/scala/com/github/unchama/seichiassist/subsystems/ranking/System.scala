package com.github.unchama.seichiassist.subsystems.ranking

import cats.effect.{Concurrent, Timer}
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.loginranking.domain.LoginTime
import com.github.unchama.seichiassist.subsystems.ranking.api.{AssortedRankingApi, RankingProvider}
import com.github.unchama.seichiassist.subsystems.ranking.application.GenericRefreshingRankingCacheV2
import com.github.unchama.seichiassist.subsystems.ranking.domain.VoteCount
import com.github.unchama.seichiassist.subsystems.ranking.infrastructure._
import io.chrisdavenport.log4cats.ErrorLogger

object System {

  import cats.implicits._

  def wired[
    F[_] : Timer : Concurrent : ErrorLogger,
    H[_]
  ]: F[AssortedRankingApi[F]] =
    for {
      seichiRanking <- GenericRefreshingRankingCacheV2.withPersistence(new JdbcSeichiRankingRecordPersistence[F])
      buildRanking <- GenericRefreshingRankingCacheV2.withPersistence(new JdbcBuildRankingRecordPersistence[F])
      loginRanking <- GenericRefreshingRankingCacheV2.withPersistence(new JdbcLoginRankingRecordPersistence[F])
      voteRanking <- GenericRefreshingRankingCacheV2.withPersistence(new JdbcVoteRankingRecordPersistence[F])
    } yield {
      new AssortedRankingApi[F] {
        override val seichiAmountRanking: RankingProvider[F, SeichiAmountData] = RankingProvider(seichiRanking)
        override val buildAmountRanking: RankingProvider[F, BuildAmountData] = RankingProvider(buildRanking)
        override val loginTimeRanking: RankingProvider[F, LoginTime] = RankingProvider(loginRanking)
        override val voteCountRanking: RankingProvider[F, VoteCount] = RankingProvider(voteRanking)
      }
    }
}
