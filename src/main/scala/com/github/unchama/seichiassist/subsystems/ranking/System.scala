package com.github.unchama.seichiassist.subsystems.ranking

import cats.effect.{Concurrent, Timer}
import com.github.unchama.seichiassist.subsystems.ranking.application.GenericRefreshingRankingCache
import com.github.unchama.seichiassist.subsystems.ranking.domain.{RankingRecordPersistence, SeichiRanking, SeichiRankingRecord}
import com.github.unchama.seichiassist.subsystems.ranking.infrastructure.JdbcRankingRecordPersistence
import io.chrisdavenport.log4cats.ErrorLogger

object System {

  import cats.implicits._

  def wired[
    F[_] : Timer : Concurrent : ErrorLogger,
    H[_]
  ]: F[RankingApi[F, SeichiRanking]] = {
    val persistence: RankingRecordPersistence[F, SeichiRankingRecord] = new JdbcRankingRecordPersistence[F]

    GenericRefreshingRankingCache
      .ofSeichiRanking
      .withPersistence(persistence)
      .map { getSeichiRankingCache =>
        new RankingApi[F, SeichiRanking] {
          override val getRanking: F[SeichiRanking] = getSeichiRankingCache
        }
      }
  }
}
