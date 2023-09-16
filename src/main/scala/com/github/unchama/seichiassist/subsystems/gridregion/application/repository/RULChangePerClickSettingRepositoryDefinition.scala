package com.github.unchama.seichiassist.subsystems.gridregion.application.repository

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.seichiassist.subsystems.gridregion.domain.RULChangePerClickSetting

object RULChangePerClickSettingRepositoryDefinition {
  def initialization[F[_]: Sync, Player]
    : TwoPhasedRepositoryInitialization[F, Player, RULChangePerClickSetting[F]] =
    TwoPhasedRepositoryInitialization
      .withoutPrefetching[F, Player, RULChangePerClickSetting[F]] { _ =>
        RULChangePerClickSetting[F]
      }

  def finalization[F[_]: Applicative, Player]
    : RepositoryFinalization[F, Player, RULChangePerClickSetting[F]] =
    RepositoryFinalization.trivial

}
