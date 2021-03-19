package com.github.unchama.datarepository.template

import cats.Applicative

import java.util.UUID

sealed trait RepositoryDefinition[F[_], Player, R]

object RepositoryDefinition {

  case class SinglePhased[F[_], Player, R](initialization: SinglePhasedRepositoryInitialization[F, R],
                                           tappingAction: (Player, R) => F[Unit],
                                           finalization: RepositoryFinalization[F, UUID, R])
    extends RepositoryDefinition[F, Player, R]

  object SinglePhased {
    def withoutTappingAction[
      F[_] : Applicative, Player, R
    ](initialization: SinglePhasedRepositoryInitialization[F, R],
      finalization: RepositoryFinalization[F, UUID, R]): SinglePhased[F, Player, R] = {
      SinglePhased(initialization, (_, _) => Applicative[F].unit, finalization)
    }
  }

  case class TwoPhased[F[_], Player, R](initialization: TwoPhasedRepositoryInitialization[F, Player, R],
                                        finalization: RepositoryFinalization[F, Player, R])
    extends RepositoryDefinition[F, Player, R]

}
