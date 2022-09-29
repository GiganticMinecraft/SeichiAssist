package com.github.unchama.seichiassist.subsystems.minestack.application.repository

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackSettings

object MineStackSettingsRepositoryDefinition {

  def initialization[F[_]: Sync, Player]
    : TwoPhasedRepositoryInitialization[F, Player, MineStackSettings[F]] =
    TwoPhasedRepositoryInitialization.withoutPrefetching[F, Player, MineStackSettings[F]] { _ =>
      Sync[F].pure(new MineStackSettings[F])
    }

  def finalization[F[_]: Applicative, Player]
    : RepositoryFinalization[F, Player, MineStackSettings[F]] = RepositoryFinalization.trivial

}
