package com.github.unchama.seichiassist.subsystems.awayscreenname.application.repository

import cats.Applicative
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.seichiassist.subsystems.awayscreenname.domain.PlayerLocationRepository

object PlayerLocationRepositoryDefinitions {

  def initialization[F[_]: Applicative, Location, Player](
    playerLocationRepository: Player => PlayerLocationRepository[F, Location, Player]
  ): TwoPhasedRepositoryInitialization[F, Player, PlayerLocationRepository] =
    TwoPhasedRepositoryInitialization.withoutPrefetching[F, Player, PlayerLocationRepository] {
      player => Applicative[F].pure(playerLocationRepository(player))
    }

  def finalization[F[_]: Applicative, Player]
    : RepositoryFinalization[F, Player, PlayerLocationRepository] =
    RepositoryFinalization.trivial

}
