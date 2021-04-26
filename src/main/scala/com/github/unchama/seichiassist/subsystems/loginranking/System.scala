package com.github.unchama.seichiassist.subsystems.loginranking

import cats.effect.{Concurrent, Timer}
import com.github.unchama.seichiassist.subsystems.loginranking.domain.{LoginTimeRanking, LoginTimeRankingRecord}
import com.github.unchama.seichiassist.subsystems.loginranking.inftastructure.JdbcReadAllLoginTimeRankingRecord
import com.github.unchama.seichiassist.subsystems.ranking.RankingApi
import com.github.unchama.seichiassist.subsystems.ranking.application.GenericRefreshingRankingCache
import com.github.unchama.seichiassist.subsystems.ranking.domain.RankingRecordPersistence
import io.chrisdavenport.log4cats.ErrorLogger

object System {
  import cats.implicits._

  def wired[
    F[_] : Timer : Concurrent : ErrorLogger
  ]: F[RankingApi[F, LoginTimeRanking]] = {
    val persistence: RankingRecordPersistence[F, LoginTimeRankingRecord] = new JdbcReadAllLoginTimeRankingRecord[F]
    GenericRefreshingRankingCache
      .ofLoginRanking
      .withPersistence(persistence)
      .map { getFromCache =>
        new RankingApi[F, LoginTimeRanking] {
          override val getRanking: F[LoginTimeRanking] = getFromCache
        }
      }
  }
}
