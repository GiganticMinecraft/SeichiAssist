package com.github.unchama.seichiassist.subsystems.gridregion.application.repository

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.seichiassist.subsystems.gridregion.domain.RegionUnitPerClickSetting

object RegionUnitPerClickSettingRepositoryDefinition {

  def initialization[F[_]: Sync, Player]
    : TwoPhasedRepositoryInitialization[F, Player, RegionUnitPerClickSetting[F]] =
    TwoPhasedRepositoryInitialization
      .withoutPrefetching[F, Player, RegionUnitPerClickSetting[F]] { _ =>
        RegionUnitPerClickSetting[F]
      }

  def finalization[F[_]: Applicative, Player]
    : RepositoryFinalization[F, Player, RegionUnitPerClickSetting[F]] =
    RepositoryFinalization.trivial

}
