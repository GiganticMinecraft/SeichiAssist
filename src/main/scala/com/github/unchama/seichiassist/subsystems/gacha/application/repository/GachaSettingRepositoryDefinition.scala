package com.github.unchama.seichiassist.subsystems.gacha.application.repository

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaDrawSettings

object GachaSettingRepositoryDefinition {

  def initialization[F[_]: Sync, Player]
    : TwoPhasedRepositoryInitialization[F, Player, GachaDrawSettings[F]] =
    TwoPhasedRepositoryInitialization.withoutPrefetching[F, Player, GachaDrawSettings[F]] { _ =>
      Sync[F].pure(new GachaDrawSettings[F])
    }

  def finalization[F[_]: Applicative, Player]
    : RepositoryFinalization[F, Player, GachaDrawSettings[F]] = RepositoryFinalization.trivial

}
