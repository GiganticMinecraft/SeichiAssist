package com.github.unchama.seichiassist.subsystems.ranking.application

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Timer}
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.seichiassist.subsystems.ranking.domain.{RankingRecordPersistence, SeichiRanking, SeichiRankingRecord}
import io.chrisdavenport.log4cats.ErrorLogger

import scala.concurrent.duration.DurationInt
import cats.implicits._
import cats.effect.implicits._
import com.github.unchama.seichiassist.subsystems.buildranking.domain.{BuildRanking, BuildRankingRecord}

class GenericRefreshingRankingCache[RR, R](recordsToRecord: Vector[RR] => R) {
  def withPersistence[F[_] : Concurrent : Timer : ErrorLogger](persistence: RankingRecordPersistence[F, RR]): F[F[R]] =
    for {
      initialRankingRecords <- persistence.getAllRankingRecords
      rankingRef <- Ref.of(recordsToRecord(initialRankingRecords))
      _ <-
        StreamExtra.compileToRestartingStream[F, Unit] {
          fs2.Stream
            .awakeEvery[F](30.seconds)
            .evalMap(_ => persistence.getAllRankingRecords)
            .evalTap(refreshedRecords => rankingRef.set(recordsToRecord(refreshedRecords)))
        }.start
    } yield {
      rankingRef.get
    }
}

object GenericRefreshingRankingCache {
  val ofSeichiRanking = new GenericRefreshingRankingCache[SeichiRankingRecord, SeichiRanking](new SeichiRanking(_))

  val ofBuildExpAmountRanking = new GenericRefreshingRankingCache[BuildRankingRecord, BuildRanking](new BuildRanking(_))
}
