package com.github.unchama.seichiassist.subsystems.voteranking

import cats.effect.{Concurrent, Timer}
import com.github.unchama.seichiassist.subsystems.ranking.RankingApi
import com.github.unchama.seichiassist.subsystems.ranking.application.GenericRefreshingRankingCache
import com.github.unchama.seichiassist.subsystems.ranking.domain.RankingRecordPersistence
import com.github.unchama.seichiassist.subsystems.voteranking.domain.{VoteCountRanking, VoteCountRankingRecord}
import com.github.unchama.seichiassist.subsystems.voteranking.infrastructure.JdbcReadAllVoteCountRankingRecord
import io.chrisdavenport.log4cats.ErrorLogger

object System {
  import cats.implicits._

  def wired[
    F[_] : Timer : Concurrent : ErrorLogger
  ]: F[RankingApi[F, VoteCountRanking]] = {
    val persistence: RankingRecordPersistence[F, VoteCountRankingRecord] = new JdbcReadAllVoteCountRankingRecord[F]
    GenericRefreshingRankingCache
      .ofVoteRanking
      .withPersistence(persistence)
      .map { getFromCache =>
        new RankingApi[F, VoteCountRanking] {
          override val getRanking: F[VoteCountRanking] = getFromCache
        }
      }
  }
}
