package com.github.unchama.seichiassist.subsystems.gridregion.application.repository

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.seichiassist.subsystems.gridregion.domain.RegionShapeSelectionState

object RegionUnitsRepositoryDefinition {

  def initialization[F[_]: Sync, Player]
    : TwoPhasedRepositoryInitialization[F, Player, RegionShapeSelectionState[F]] =
    TwoPhasedRepositoryInitialization
      .withoutPrefetching[F, Player, RegionShapeSelectionState[F]] { _ =>
        RegionShapeSelectionState[F]
      }

  def finalization[F[_]: Applicative, Player]
    : RepositoryFinalization[F, Player, RegionShapeSelectionState[F]] =
    RepositoryFinalization.trivial

}
