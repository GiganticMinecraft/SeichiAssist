package com.github.unchama.seichiassist.subsystems.ranking.application

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Timer}
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.seichiassist.subsystems.ranking.domain.{RankingRecordPersistence, SeichiRanking}
import io.chrisdavenport.log4cats.ErrorLogger

object RefreshingRankingCache {

  import cats.effect.implicits._
  import cats.implicits._

  import scala.concurrent.duration._

  def withPersistence[F[_] : Concurrent : Timer : ErrorLogger](persistence: RankingRecordPersistence[F]): F[F[SeichiRanking]] =
    for {
      initialRankingRecords <- persistence.getAllRankingRecords
      rankingRef <- Ref.of(new SeichiRanking(initialRankingRecords))
      _ <-
        StreamExtra.compileToRestartingStream[F, Unit] {
          fs2.Stream
            .awakeEvery[F](30.seconds)
            .evalMap(_ => persistence.getAllRankingRecords)
            .evalTap(refreshedRecords => rankingRef.set(new SeichiRanking(refreshedRecords)))
        }.start
    } yield {
      rankingRef.get
    }

}
