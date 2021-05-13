package com.github.unchama.seichiassist.subsystems.ranking.application

import cats.Order
import cats.effect.concurrent.Ref
import cats.effect.implicits._
import cats.effect.{Concurrent, Timer}
import cats.implicits._
import cats.kernel.Monoid
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.seichiassist.subsystems.buildranking.domain.{BuildRanking, BuildRankingRecord}
import com.github.unchama.seichiassist.subsystems.loginranking.domain.{LoginTimeRanking, LoginTimeRankingRecord}
import com.github.unchama.seichiassist.subsystems.ranking.domain._
import com.github.unchama.seichiassist.subsystems.voteranking.domain.{VoteCountRanking, VoteCountRankingRecord}
import io.chrisdavenport.log4cats.ErrorLogger

import scala.concurrent.duration.DurationInt

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

object GenericRefreshingRankingCacheV2 {

  def withPersistence[
    F[_] : Concurrent : Timer : ErrorLogger, R: Order: Monoid
  ](persistence: GenericRankingRecordPersistence[F, R]): F[ReadOnlyRef[F, Ranking[R]]] =
    for {
      initialRankingRecords <- persistence.getAllRankingRecords
      rankingRef <- Ref.of(new Ranking(initialRankingRecords))
      _ <-
        StreamExtra.compileToRestartingStream[F, Unit] {
          fs2.Stream
            .awakeEvery[F](30.seconds)
            .evalMap(_ => persistence.getAllRankingRecords)
            .evalTap(newRecords => rankingRef.set(new Ranking(newRecords)))
        }.start
    } yield ReadOnlyRef.fromRef(rankingRef)

}

object GenericRefreshingRankingCache {
  val ofSeichiRanking = new GenericRefreshingRankingCache[SeichiRankingRecord, SeichiRanking](new SeichiRanking(_))

  val ofBuildExpAmountRanking = new GenericRefreshingRankingCache[BuildRankingRecord, BuildRanking](new BuildRanking(_))

  val ofLoginRanking = new GenericRefreshingRankingCache[LoginTimeRankingRecord, LoginTimeRanking](new LoginTimeRanking(_))

  val ofVoteRanking = new GenericRefreshingRankingCache[VoteCountRankingRecord, VoteCountRanking](new VoteCountRanking(_))
}
