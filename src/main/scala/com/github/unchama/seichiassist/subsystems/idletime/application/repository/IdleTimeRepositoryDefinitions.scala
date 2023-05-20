package com.github.unchama.seichiassist.subsystems.idletime.application.repository

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.seichiassist.subsystems.idletime.domain.PlayerIdleMinuteRepository

object IdleTimeRepositoryDefinitions {

  def initialization[F[_]: Sync, Player]
    : TwoPhasedRepositoryInitialization[F, Player, PlayerIdleMinuteRepository[F]] =
    TwoPhasedRepositoryInitialization
      .withoutPrefetching[F, Player, PlayerIdleMinuteRepository[F]] { _ =>
        Sync[F].pure(new PlayerIdleMinuteRepository[F])
      }

  def finalization[F[_]: Applicative, Player]
    : RepositoryFinalization[F, Player, PlayerIdleMinuteRepository[F]] =
    RepositoryFinalization.trivial

}
