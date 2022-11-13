package com.github.unchama.seichiassist.subsystems.gridregion.application.repository

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.seichiassist.subsystems.gridregion.domain.RegionUnitsSetting

object RegionUnitsRepositoryDefinition {

  def initialization[F[_]: Sync, Player]
    : TwoPhasedRepositoryInitialization[F, Player, RegionUnitsSetting[F]] =
    TwoPhasedRepositoryInitialization.withoutPrefetching[F, Player, RegionUnitsSetting[F]] {
      _ => Sync[F].pure(new RegionUnitsSetting[F])
    }

  def finalization[F[_]: Applicative, Player]
    : RepositoryFinalization[F, Player, RegionUnitsSetting[F]] =
    RepositoryFinalization.trivial

}
