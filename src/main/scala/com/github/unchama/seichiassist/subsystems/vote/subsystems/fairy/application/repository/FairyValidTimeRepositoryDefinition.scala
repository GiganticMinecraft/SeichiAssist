package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.{Applicative, Monad}
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{
  FairyValidTimes,
  FairyValidTimesState
}

object FairyValidTimeRepositoryDefinition {

  def initialization[F[_]: Sync: Monad, Player](
    implicit fairyValidTimesState: FairyValidTimesState[F]
  ): TwoPhasedRepositoryInitialization[F, Player, Ref[F, Option[FairyValidTimes]]] =
    TwoPhasedRepositoryInitialization
      .withoutPrefetching[F, Player, Ref[F, Option[FairyValidTimes]]] { _ =>
        Sync[F].pure(fairyValidTimesState.fairyValidTimes)
      }

  def finalization[F[_]: Applicative, Player]
    : RepositoryFinalization[F, Player, Ref[F, Option[FairyValidTimes]]] =
    RepositoryFinalization.trivial

}
