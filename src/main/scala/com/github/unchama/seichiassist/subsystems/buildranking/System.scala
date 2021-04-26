package com.github.unchama.seichiassist.subsystems.buildranking

import cats.effect.{Concurrent, Timer}
import com.github.unchama.seichiassist.subsystems.buildranking.domain.{BuildRanking, BuildRankingRecord}
import com.github.unchama.seichiassist.subsystems.buildranking.infrastructure.JdbcReadAllBuildAmountData
import com.github.unchama.seichiassist.subsystems.ranking.RankingApi
import com.github.unchama.seichiassist.subsystems.ranking.application.GenericRefreshingRankingCache
import com.github.unchama.seichiassist.subsystems.ranking.domain.RankingRecordPersistence
import io.chrisdavenport.log4cats.ErrorLogger

object System {
  import cats.implicits._

  def wired[
    F[_] : Timer : Concurrent : ErrorLogger
  ]: F[RankingApi[F, BuildRanking]] = {
    val persistence: RankingRecordPersistence[F, BuildRankingRecord] = new JdbcReadAllBuildAmountData[F]
    // TODO 抽象化できる余地があるかもしれない
    GenericRefreshingRankingCache
      .ofBuildExpAmountRanking
      .withPersistence(persistence)
      .map { getFromCache =>
        new RankingApi[F, BuildRanking] {
          override val getRanking: F[BuildRanking] = getFromCache
        }
      }
  }
}
