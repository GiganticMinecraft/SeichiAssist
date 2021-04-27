package com.github.unchama.seichiassist.subsystems.ranking

import cats.effect.{Concurrent, Timer}
import com.github.unchama.seichiassist.subsystems.ranking.application.RefreshingRankingCache
import com.github.unchama.seichiassist.subsystems.ranking.domain.{RankingRecordPersistence, SeichiRanking}
import com.github.unchama.seichiassist.subsystems.ranking.infrastructure.JdbcRankingRecordPersistence
import io.chrisdavenport.log4cats.ErrorLogger

object System {

  import cats.implicits._

  def wired[
    F[_] : Timer : Concurrent : ErrorLogger,
    H[_]
  ]: F[RankingApi[F]] = {
    val persistence: RankingRecordPersistence[F] = new JdbcRankingRecordPersistence[F]

    RefreshingRankingCache
      .withPersistence(persistence)
      .map { getSeichiRankingCache =>
        new RankingApi[F] {
          override val getSeichiRanking: F[SeichiRanking] = getSeichiRankingCache
        }
      }
  }
}
