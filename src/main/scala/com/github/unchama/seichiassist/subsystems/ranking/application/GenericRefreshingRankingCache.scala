package com.github.unchama.seichiassist.subsystems.ranking.application

import cats.Order
import cats.effect.concurrent.Ref
import cats.effect.implicits._
import cats.effect.{Concurrent, Timer}
import cats.implicits._
import cats.kernel.Monoid
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.seichiassist.subsystems.ranking.domain._
import io.chrisdavenport.log4cats.ErrorLogger

import scala.concurrent.duration.DurationInt

object GenericRefreshingRankingCache {

  def withPersistence[F[_]: Concurrent: Timer: ErrorLogger, R: Order: Monoid](
    persistence: RankingRecordPersistence[F, R]
  ): F[ReadOnlyRef[F, Ranking[R]]] =
    for {
      initialRankingRecords <- persistence.getAllRankingRecords
      rankingRef <- Ref.of(new Ranking(initialRankingRecords))
      _ <-
        StreamExtra
          .compileToRestartingStream[F, Unit]("[GenericRefreshingRankingCache]") {
            fs2
              .Stream
              .awakeEvery[F](30.seconds)
              .evalMap(_ => persistence.getAllRankingRecords)
              .evalTap(newRecords => rankingRef.set(new Ranking(newRecords)))
          }
          .start
    } yield ReadOnlyRef.fromRef(rankingRef)

}
