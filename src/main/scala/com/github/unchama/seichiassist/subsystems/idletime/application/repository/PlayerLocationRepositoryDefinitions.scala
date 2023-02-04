package com.github.unchama.seichiassist.subsystems.idletime.application.repository

import cats.Applicative
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.seichiassist.subsystems.idletime.domain.PlayerLocationRepository

object PlayerLocationRepositoryDefinitions {

  def initialization[F[_]: Applicative, Location, Player](
    implicit playerLocationRepository: Player => PlayerLocationRepository[F, Location, Player]
  ): TwoPhasedRepositoryInitialization[F, Player, PlayerLocationRepository[
    F,
    Location,
    Player
  ]] =
    TwoPhasedRepositoryInitialization
      .withoutPrefetching[F, Player, PlayerLocationRepository[F, Location, Player]] { player =>
        Applicative[F].pure(playerLocationRepository(player))
      }

  def finalization[F[_]: Applicative, Location, Player]
    : RepositoryFinalization[F, Player, PlayerLocationRepository[F, Location, Player]] =
    RepositoryFinalization.trivial

}
