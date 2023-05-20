package com.github.unchama.seichiassist.subsystems.minestack.application.repository

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackSettings
import com.github.unchama.seichiassist.subsystems.minestack.domain.persistence.PlayerSettingPersistence

object MineStackSettingsRepositoryDefinition {

  def initialization[F[_]: Sync, Player: HasUuid](
    implicit playerSettingPersistence: PlayerSettingPersistence[F]
  ): TwoPhasedRepositoryInitialization[F, Player, MineStackSettings[F, Player]] =
    TwoPhasedRepositoryInitialization
      .withoutPrefetching[F, Player, MineStackSettings[F, Player]] { player =>
        Sync[F].pure(new MineStackSettings[F, Player](player))
      }

  def finalization[F[_]: Applicative, Player]
    : RepositoryFinalization[F, Player, MineStackSettings[F, Player]] =
    RepositoryFinalization.trivial

}
